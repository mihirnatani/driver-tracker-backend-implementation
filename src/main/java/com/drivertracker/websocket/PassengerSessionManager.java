package com.drivertracker.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PassengerSessionManager {

    // driverId → set of all passenger sessions watching that driver
    private final ConcurrentHashMap<String, Set<WebSocketSession>> driverSessions
            = new ConcurrentHashMap<>();

    // Called when a passenger connects and says "I want to watch driver X"
    public void addSession(String driverId, WebSocketSession session) {
        driverSessions
                .computeIfAbsent(driverId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(session);
    }

    // Called when a passenger disconnects
    public void removeSession(String driverId, WebSocketSession session) {
        Set<WebSocketSession> sessions = driverSessions.get(driverId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                driverSessions.remove(driverId);
            }
        }
    }

    // Called when we want to push a location update to all watchers of a driver
    public Set<WebSocketSession> getSessions(String driverId) {
        return driverSessions.getOrDefault(driverId, Collections.emptySet());
    }
}