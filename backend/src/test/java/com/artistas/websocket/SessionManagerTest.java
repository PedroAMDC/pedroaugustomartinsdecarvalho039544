package com.artistas.websocket;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@ExtendWith(MockitoExtension.class)
public class SessionManagerTest {

    @Inject
    SessionManager sessionManager;

    @Test
    void getAllSessions_shouldReturnCollectionFromOpenConnections() {
        // The actual connections are managed by Quarkus OpenConnections
        // This test verifies the method doesn't throw and returns a collection
        Collection<WebSocketConnection> sessions = sessionManager.getAllSessions();
        assertNotNull(sessions);
    }

    @Test
    void getSessionCount_shouldReturnCountFromOpenConnections() {
        // Without active WebSocket connections, count should be 0 or more
        int count = sessionManager.getSessionCount();
        assertTrue(count >= 0);
    }

    @Test
    void broadcast_withNoSessions_shouldNotThrowException() {
        assertDoesNotThrow(() -> sessionManager.broadcast("test message"));
    }

    @Test
    void broadcastExcept_withNoSessions_shouldNotThrowException() {
        assertDoesNotThrow(() -> sessionManager.broadcastExcept("test message", "excluded-id"));
    }
}
