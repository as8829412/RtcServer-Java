package com.tillo.rtc.model;

import com.tillo.rtc.handler.MyWebSocket;

import javax.websocket.Session;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Room {

    public String room_id;

    Map<String,Client> clientIds;
    Map<String,Session> sessionMap;

    public Room() {
    }


    public Map<String, Session> getSessionMap() {
        return sessionMap;
    }

    public void setSessionMap(Map<String, Session> sessionMap) {
        this.sessionMap = sessionMap;
    }
    public Room setSession(String clientId,Session session){
        if (Objects.isNull(this.sessionMap)){
            this.sessionMap=new ConcurrentHashMap<String, Session>();
        }
        this.sessionMap.put(clientId,session);
        return this;
    }
    public Map<String, Client> getClientIds() {
        return clientIds;
    }

    public void setClientIds(Map<String, Client> clientIds) {
        this.clientIds = clientIds;
    }
    public Room addOneClient(String client_id,Client client){
        if (Objects.isNull(clientIds)){
            this.clientIds=new ConcurrentHashMap<String, Client>();
        }
        this.clientIds.put(client_id,client);
        return this;
    }
    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    @Override
    public String toString() {
        return "Room{" +
                "room_id='" + room_id + '\'' +
                ", clientIds=" + clientIds +
                '}';
    }

}
