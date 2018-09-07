package com.tillo.rtc.service;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

public interface RoomService {

    public String add_client_to_room(HttpServletRequest request,String room_id,String client_id);

    public String save_message_from_client(HttpServletRequest request,String room_id,String client_id,String msg );

    public String remove_client_from_room(HttpServletRequest request,String room_id,String client_id);
}
