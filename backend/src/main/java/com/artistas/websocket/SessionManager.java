package com.artistas.websocket;

import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;

@ApplicationScoped
public class SessionManager {

    @Inject
    OpenConnections openConnections;

    public Collection<WebSocketConnection> getAllSessions() {
        return openConnections.listAll();
    }

    public int getSessionCount() {
        return openConnections.listAll().size();
    }

    public void broadcast(String message) {
        openConnections.listAll().forEach(connection -> {
            try {
                connection.sendTextAndAwait(message);
            } catch (Exception e) {
                Log.warnf("Failed to send message to connection %s: %s",
                    connection.id(), e.getMessage());
            }
        });
    }

    public void broadcastExcept(String message, String excludeConnectionId) {
        openConnections.listAll().stream()
            .filter(conn -> !conn.id().equals(excludeConnectionId))
            .forEach(conn -> {
                try {
                    conn.sendTextAndAwait(message);
                } catch (Exception e) {
                    Log.warnf("Failed to send message to connection %s: %s",
                        conn.id(), e.getMessage());
                }
            });
    }
}
