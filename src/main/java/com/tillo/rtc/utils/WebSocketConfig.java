package com.tillo.rtc.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 如果不是spring boot项目，那就不需要进行这样的配置，因为如果在tomcat中运行的话，
 * tomcat会扫描带有@ServerEndpoint的注解成为websocket，而spring boot项目中需要由这个bean来提供注册管理
 */
@Configuration
public class WebSocketConfig {
   /* public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/ws01").setViewName("/ws01");
    }*/
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
