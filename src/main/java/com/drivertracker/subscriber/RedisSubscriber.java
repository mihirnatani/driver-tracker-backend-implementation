package com.drivertracker.subscriber;

import com.drivertracker.websocket.PassengerSessionManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

@Component
public class RedisSubscriber implements MessageListener {

    private final PassengerSessionManager sessionManager;

    public RedisSubscriber(PassengerSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

        // 1. Extract the channel name e.g. "driver:location:driver123"
        String channel = new String(message.getChannel());

        // 2. Extract driverId from channel name
        String driverId = channel.replace("driver:location:", "");

        // 3. The actual location JSON payload
        String locationJson = new String(message.getBody());

        // 4. Find all passengers watching this driver
        Set<WebSocketSession> sessions = sessionManager.getSessions(driverId);

        // 5. Push location to each connected passenger
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(locationJson));
                } catch (IOException e) {
                    System.out.println("Failed to send to session: " + session.getId());
                }
            }
        }
    }
}