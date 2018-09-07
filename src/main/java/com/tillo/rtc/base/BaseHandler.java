package com.tillo.rtc.base;

import com.tillo.rtc.model.Room;
import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseHandler {
    //存放房间id和房间
    public static Map<String,Room> clientMap=new ConcurrentHashMap<String, Room>();

    public static Map<String,Session> sessionMap=new HashMap<String, Session>();


}
