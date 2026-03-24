package com.drivertracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.drivertracker.websocket.PassengerWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PassengerWebSocketHandler passengerHandler;

    public WebSocketConfig(PassengerWebSocketHandler passengerHandler) {
        this.passengerHandler = passengerHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(passengerHandler, "/track/{driverId}")
                .setAllowedOrigins("*");
    }
}