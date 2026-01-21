package com.artistas.websocket;

import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SessionManager {

    private final Map<String, WebSocketConnection> sessions = new ConcurrentHashMap<>();

    public void addSession(String connectionId, WebSocketConnection connection) {
        sessions.put(connectionId, connection);
    }

    public void removeSession(String connectionId) {
        sessions.remove(connectionId);
    }

    public WebSocketConnection getSession(String connectionId) {
        return sessions.get(connectionId);
    }

    public Collection<WebSocketConnection> getAllSessions() {
        return sessions.values();
    }

    public int getSessionCount() {
        return sessions.size();
    }

    public boolean hasSession(String connectionId) {
        return sessions.containsKey(connectionId);
    }

    public void broadcast(String message) {
        sessions.values().forEach(connection -> {
            connection.sendTextAndAwait(message);
        });
    }

    public void broadcastExcept(String message, String excludeConnectionId) {
        sessions.entrySet().stream()
            .filter(entry -> !entry.getKey().equals(excludeConnectionId))
            .forEach(entry -> entry.getValue().sendTextAndAwait(message));
    }

    public void clearAll() {
        sessions.clear();
    }
}
