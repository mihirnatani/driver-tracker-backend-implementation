package com.drivertracker.websocket;

import com.drivertracker.security.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class PassengerWebSocketHandler extends TextWebSocketHandler {

    private final PassengerSessionManager sessionManager;
    private final JwtUtil jwtUtil;

    public PassengerWebSocketHandler(PassengerSessionManager sessionManager,
                                     JwtUtil jwtUtil) {
        this.sessionManager = sessionManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 1. Extract token from query param
        String token = extractToken(session);

        // 2. Validate token and role
        if (token == null || !jwtUtil.isTokenValid(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            System.out.println("WebSocket rejected - invalid token");
            return;
        }

        String role = jwtUtil.extractRole(token);
        if (!"PASSENGER".equals(role)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            System.out.println("WebSocket rejected - not a passenger");
            return;
        }

        // 3. Valid passenger - register session
        String driverId = extractDriverId(session);
        sessionManager.addSession(driverId, session);
        System.out.println("Passenger connected. Watching driver: " + driverId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String driverId = extractDriverId(session);
        sessionManager.removeSession(driverId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // passengers only receive
    }

    private String extractToken(WebSocketSession session) {
        String query = session.getUri().getQuery(); // "token=eyJhbG..."
        if (query != null && query.startsWith("token=")) {
            return query.substring(6);
        }
        return null;
    }

    private String extractDriverId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
