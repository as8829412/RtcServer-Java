package com.tillo.rtc.controller;

import com.tillo.rtc.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class RoomController {
    @Autowired
    private RoomService rms;

    @RequestMapping("/join")
    public String JoinPage(HttpServletRequest request, HttpServletResponse response){
        String room_id = request.getParameter("roomId");
        String client_id =request.getParameter("clientId");
        String params=rms.add_client_to_room(request,room_id,client_id);
        return params;
    }
    @RequestMapping("/message")
    public String Message(HttpServletRequest request, HttpServletResponse response){
        String roomId = request.getParameter("roomId");
        String client =request.getParameter("clientId");
        String msg =request.getParameter("msg");
        String params = rms.save_message_from_client(request,roomId,client,msg);
        return params;
    }
    @RequestMapping("/leave")
    public String Leave(HttpServletRequest request, HttpServletResponse response){
        String roomId = request.getParameter("roomId");
        String client =request.getParameter("clientId");
        String params = rms.remove_client_from_room(request,roomId,client);
        return params;
    }
}
