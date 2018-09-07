package com.tillo.rtc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebSocketController {
    @RequestMapping("index")
    public String index(){
        return "index.html";
    }
}
