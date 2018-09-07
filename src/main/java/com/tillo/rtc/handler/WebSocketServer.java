package com.tillo.rtc.handler;

//import org.apache.juli.logging.Log;
//import org.apache.juli.logging.LogFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tillo.rtc.base.BaseHandler;
import com.tillo.rtc.dto.MessageRequest;
import com.tillo.rtc.dto.MessageResponse;
import com.tillo.rtc.model.Room;
import com.tillo.rtc.utils.Constants;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
/**
 * ServerEndpoint
 * <p>
 * 使用springboot的唯一区别是要@Component声明下，而使用独立容器是由容器自己管理websocket的，但在springboot中连容器都是spring管理的。
 * <p>
 * 虽然@Component默认是单例模式的，但springboot还是会为每个websocket连接初始化一个bean，所以可以用一个静态set保存起来。
 */
@ServerEndpoint("/websocket/{sid}")
@Component
public class WebSocketServer {

    private static Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private static final long MAX_TIME_OUT = 2 * 60 * 1000;

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
    //用来记录userId和该session进行绑定
    private static Map<String,Session> sessionMap=new ConcurrentHashMap<String, Session>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //房间队列，用于存放用户和房间
    Map<String,Room> clientMap=BaseHandler.clientMap;
    //接收sid
    private String sid="";
    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session,@PathParam("sid") String sid) {
        this.session = session;
        String msg="";
        if (!sessionMap.containsKey(sid)){
            webSocketSet.add(this);     //加入set中
            addOnlineCount();           //在线数加1
            this.sid=sid;
            sessionMap.put(sid,session);
            //session.setMaxIdleTimeout(MAX_TIME_OUT);
            log.info("有新窗口开始监听:"+sid+",当前在线人数为" + getOnlineCount()+",session:"+sessionMap.get(sid).getId());
            msg="Successful connection";
        }else{
            msg="client already exit";
        }
        try {
            //this.session.getAsyncRemote().sendText(gson.toJson(msgList));
            sendMessage(msg+"=="+sid);
        } catch (IOException e) {
            log.error("websocket IO异常");
            //this.session.getAsyncRemote().sendText("该用户没有聊天记录");
        }
    }
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        sessionMap.remove(this.sid);
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount()+",断连对象:"+this.sid);
    }
    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        ObjectMapper objectMapper = new ObjectMapper();
        MessageRequest msg;
        String error="";
        try {
            msg=objectMapper.readValue(message, MessageRequest.class);
            log.info("收到来自用户"+sid+"的信息:"+msg.toString()+",session:"+session.getId());

            switch (msg.getCmd()){
                case "join":
                    if (msg.getRoomid() ==""||msg.getRoomid()=="null"){
                        error="RoomID is null";
                        break;
                    }
                    if (msg.getClientid() ==""||msg.getClientid()=="null") {
                        error = "ClientID is null";
                        break;
                    }
                    boolean inRoom=this.JoinRoom(msg.getRoomid(),msg.getClientid());
                    break;
                case "call":
                    if (msg.getToUser()==""){
                        error ="toUser is miss";
                        break;
                    }
                    //判断是否有用户session存在连接
                    if (!sessionMap.containsKey(msg.getToUser())){
                        error="toUser:"+msg.getToUser()+" is not registered";
                        break;
                    }
                    //判断房间是否存在用户session
                    if (!clientMap.get(msg.getRoomid()).getSessionMap().containsKey(msg.getToUser())){
                        error ="toUser:"+msg.getToUser()+" is not join room";
                        break;
                    }
                    sendTo("incomingCall",msg.getToUser());
                    break;
                case "incomingCallResponse":
                    if (msg.getMsg()==""){
                        error ="msg is miss";
                        break;
                    }
                    if (!sessionMap.containsKey(msg.getToUser())){
                        error="toUser:"+msg.getToUser()+" is not registered";
                        break;
                    }
                    //判断房间是否存在用户session
                    if (!clientMap.get(msg.getRoomid()).getSessionMap().containsKey(msg.getToUser())){
                        error ="toUser:"+msg.getToUser()+" is not join room";
                        break;
                    }
                    sendTo(msg.getMsg(),msg.getToUser());
                    break;
                case "send":
                    if (msg.getMsg()==""){
                        error ="msg is miss";
                        break;
                    }
                    if (!sessionMap.containsKey(msg.getToUser())){
                        error="toUser is null";
                        break;
                    }
                    //判断房间是否存在用户session
                    if (!clientMap.get(msg.getRoomid()).getSessionMap().containsKey(msg.getToUser())){
                        error ="toUser:"+msg.getToUser()+" is not join room";
                        break;
                    }
                    sendTo(msg.getMsg(),msg.getToUser());
                    break;
                default:
                    onClose();
                    break;
            }
            if (error!=""){
                sendInfo(error, msg.getClientid());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //群发消息
       /* for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }


    private boolean JoinRoom(String room_id,String client_id){
        boolean bool=true;
        if (clientMap.size()>0&&clientMap.get(room_id).getSessionMap()!=null&&clientMap.get(room_id).getSessionMap().size()>=2){
            bool=false;
        }else
            clientMap.get(room_id).setSession(client_id,this.session);
        System.out.println("sessionMap:"+clientMap.get(room_id).getSessionMap().values()+""+clientMap.get(room_id).getSessionMap().size());
        return bool;
    }
    private void sendTo(String message,String toUser){
      /*  JSONObject json = JSONObject.fromObject(message);
        if (json != null) {
            String type = json.getString("type");
            if ("bye".equals(type)) {//客户端退出视频聊天

            }
        }*/
        for (WebSocketServer item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                if(item.sid.equals(toUser)){
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                continue;
            }
        }
    }
    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }
    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        MessageResponse msgResponse=new MessageResponse();
        msgResponse.setMsg(message);
        Gson gs=new Gson();
        String msg=gs.toJson(msgResponse);//msgResponse.toString();
        this.session.getBasicRemote().sendText(msg);
    }

    /**
     * 群发自定义消息
     * */
    public static void sendInfo(String message,@PathParam("sid") String sid) throws IOException {
        log.info("推送消息到窗口"+sid+"，推送内容:"+message);
        for (WebSocketServer item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                if(sid==null) {
                    item.sendMessage(message);
                }else if(item.sid.equals(sid)){
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

}
