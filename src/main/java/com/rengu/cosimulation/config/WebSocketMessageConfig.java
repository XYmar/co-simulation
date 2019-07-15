package com.rengu.cosimulation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Author: XYmar
 * Date: 2019/4/10 14:35
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // config.enableSimpleBroker("/deviceInfo", "/onlineDevice", "/deployProgress");
        config.enableSimpleBroker("/userInfo", "/personalInfo", "/onlineDevice", "/deviceInfo", "/deployProgress", "/audit");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/COSIMULATION").setAllowedOrigins("*").withSockJS();
    }

}