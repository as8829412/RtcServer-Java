package com.tillo.rtc.service.serviceImpl;

import com.danga.MemCached.MemCachedClient;
import com.google.gson.Gson;
import com.google.gson.*;
import com.tillo.rtc.base.BaseHandler;
import com.tillo.rtc.handler.WebSocketServer;
import com.tillo.rtc.model.Client;
import com.tillo.rtc.model.Room;
import com.tillo.rtc.service.RoomService;
import com.tillo.rtc.utils.Constants;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Resources;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Repository("RoomServiceImpl")
public class RoomServiceImpl  implements RoomService {
    Map<String,Room> clientMap=BaseHandler.clientMap;
    //public  Map<String,Room> clientMap=new ConcurrentHashMap<>();
    @Autowired
    WebSocketServer ws;

    @Override
    public String add_client_to_room(HttpServletRequest request, String room_id, String client_id) {
        String error = "";
        JSONObject params=new JSONObject();
        //MemCachedClient memcache_client = new MemCachedClient();
        boolean isInitiator=false;
        Room room=null;
        Client client=null;
        int csize=0;
        List<String> message = new ArrayList<>();
        //Map<String,Client> clientIds=null;
        if (room_id!=""&&room_id!=null){
            if (!clientMap.containsKey(room_id)){
                room=new Room();
            }else{
                room = clientMap.get(room_id);
                csize=room.getClientIds().size();
            }
            if (null!=room.getClientIds() && csize>=2) {
                error = "ROOM_FULL";
            }
            if (room.getClientIds()!=null&&room.getClientIds().containsKey(client_id)) {
                error = "UPLICATE_CLIENT";
            }
            if (error==""){
                if (csize==0){
                    isInitiator=true;
                    room.setRoom_id(room_id);
                    client=new Client();
                    client.setClient_id(client_id);
                    client.setInitiator(isInitiator);
                    room.addOneClient(client_id,client);
                    clientMap.put(room_id,room);
                }else{
                    isInitiator=false;
                    String other=getOtherClient(room_id,client_id);
                    Client other_client=room.getClientIds().get(other);
                    message.addAll(other_client.getMessage());
                    //message=other_client.getMessage();
                    client=new Client();
                    client.setClient_id(client_id);
                    client.setInitiator(isInitiator);
                    //client.addOneMessage(message);
                    room.getClientIds().put(client_id,client);
                    other_client.getMessage().clear();
                }
            }
            System.out.println("message:"+message.toString());
        }else {
            error="null";
        }
        if (error!=""){
            return "{\"result\":\""+error+"\",\"params\":\"{}\"}";
        }else {
            params = this.get_room_parameters(request, room_id, client_id, isInitiator);
            params.put("messages",message);
        }
        System.out.println("room_state--》"+clientMap.get(room_id).getClientIds().keySet().toString());

        System.out.println("size:"+clientMap.size());
        return "{\"result\":\"SUCCESS\",\"params\":\""+params+"\"}";
    }
    private String getOtherClient(String roomid,String clientId){
        String otherClient="";
        for (String key:clientMap.get(roomid).getClientIds().keySet()){
            if (!clientMap.get(roomid).getClientIds().containsKey(clientId)){
                otherClient=key;
            }
        }
        return otherClient;
    }
    @Override
    public String save_message_from_client(HttpServletRequest request, String room_id, String client_id, String msg) {
        String error="";
        Client client=null;
        //List<String> message = new ArrayList<>();
        int retries = 0;
        boolean saved=true;
        if (room_id!=""&&room_id!=null){
            if (clientMap.get(room_id).getClientIds().containsKey(client_id)) {
                if (clientMap.get(room_id).getClientIds().size()>1) {
                    saved=false;
                }else{
                    client=clientMap.get(room_id).getClientIds().get(client_id);
                    client.addOneMessage(msg);
                    retries = retries + 1;
                    System.out.println("clientmessage:"+client.getMessage());
                }
            }else
                error= "UNKNOWN_CLIENT";
        }else
            error="UNKNOWN_ROOM";
        if (error!="")
            return "{\"result\":\""+error+"\"}";
        else {
            if (!saved){
               // send_message_to_websocket(room_id,client_id,msg);
                return "{\"result\":\"SUCCESS\"}";
            }else
                return "{\"result\":\"SUCCESS\"}";
        }
    }
    private void send_message_to_websocket(String room_id,String client_id,String message){
        try {
            ws.sendMessage(message);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public String remove_client_from_room(HttpServletRequest request, String room_id, String client_id) {
        String error ="";
        if (room_id!=""&&room_id!=null){
            if (clientMap.containsKey(room_id)){
                if (clientMap.get(room_id).getClientIds().containsKey(client_id)) {
                    clientMap.get(room_id).getClientIds().remove(client_id);
                    clientMap.get(room_id).getSessionMap().remove(client_id);
                }else
                    error= "UNKNOWN_CLIENT";
                if (clientMap.get(room_id).getClientIds().size()>0){
                    clientMap.get(room_id).getClientIds().get(getOtherClient(room_id,client_id)).setInitiator(true);
                }else {
                    clientMap.remove(room_id);
                }
            }else
                error="UNKNOWN_ROOM";
        }
        if (error!=""){
            return "{\"result\":\""+error+"\"}";
        }else {
            return "{\"result\":\"SUCCESS\"}";
        }
    }

    private JSONObject get_room_parameters(HttpServletRequest request,String room_id,String client_id,boolean isInitiator){
        JSONObject params=new JSONObject();
//        ice_transports = request.get('it')
//        ice_server_transports = request.get('tt')
//        JSONObject pc_config=make_pc_config(ice_transports, ice_server_override);
//        //控制各种网络特性的选项
//        dtls = request.get('dtls')
//        dscp = request.get('dscp')
//        ipv6 = request.get('ipv6')
//        pc_constraints = make_pc_constraints
//        params.put("pc_config",pc_config);
//        params.put("pc_constraints",pc_constraints);
        params.put("ice_server_url","https://rtc.tlifang.com/iceconfig?key=none");
        params.put("wss_url","ws://localhost:8080/websocket");
        params.put("wss_post_url","http://localhost:8080/websocket");

        if (room_id!=""&&room_id!=null) {
            String room_link = request.getRequestURL() + "/r/" + room_id;
            params.put("room_id",room_id);
            params.put("room_link",room_link);
        }
        if (client_id!=""&&client_id!=null) {
            params.put("client_id",client_id);
        }
        params.put("is_initiator",isInitiator);

        return params;
    }

    private JsonObject make_pc_config(String ice_transports,String ice_server_override){
        JsonObject config=new JsonObject();
        JsonArray array=new JsonArray();
        config.add("iceServers",array);
        config.addProperty("bundlePolicy","max-bundle");
        config.addProperty("rtcpMuxPolicy","require");
        if (ice_server_override!=""&&ice_server_override!=null)
            config.addProperty("iceServers",ice_server_override);
        if (ice_transports!=""&&ice_transports!=null)
            config.addProperty("ice_transports",ice_transports);
        return config;
    }
    /*private String make_pc_constraints(String dtls,String dscp,String ipv6){
       *//* constraints = {'optional': []};
        maybe_add_constraint(constraints, dtls, 'DtlsSrtpKeyAgreement')
        maybe_add_constraint(constraints, dscp, 'googDscp')
        maybe_add_constraint(constraints, ipv6, 'googIPv6')

        return constraints*//*
       return "";
    }
    maybe_add_constraint(constraints, param, constraint):
  if (param.lower() == 'true'):
    constraints['optional'].append({constraint: True})
  elif (param.lower() == 'false'):
    constraints['optional'].append({constraint: False})

  return constraints
    */




}
