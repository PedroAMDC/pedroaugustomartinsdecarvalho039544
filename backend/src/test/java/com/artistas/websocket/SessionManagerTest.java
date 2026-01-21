package com.artistas.websocket;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class SessionManagerTest {

    @Inject
    SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager.clearAll();
    }

    @Test
    void addSession_shouldAddConnectionToManager() {
        WebSocketConnection mockConnection = mock(WebSocketConnection.class);
        String connectionId = "conn-123";

        sessionManager.addSession(connectionId, mockConnection);

        assertTrue(sessionManager.hasSession(connectionId));
        assertEquals(1, sessionManager.getSessionCount());
    }

    @Test
    void removeSession_shouldRemoveConnectionFromManager() {
        WebSocketConnection mockConnection = mock(WebSocketConnection.class);
        String connectionId = "conn-123";
        sessionManager.addSession(connectionId, mockConnection);

        sessionManager.removeSession(connectionId);

        assertFalse(sessionManager.hasSession(connectionId));
        assertEquals(0, sessionManager.getSessionCount());
    }

    @Test
    void getSession_shouldReturnCorrectConnection() {
        WebSocketConnection mockConnection = mock(WebSocketConnection.class);
        String connectionId = "conn-123";
        sessionManager.addSession(connectionId, mockConnection);

        WebSocketConnection retrieved = sessionManager.getSession(connectionId);

        assertSame(mockConnection, retrieved);
    }

    @Test
    void getSession_withNonExistentId_shouldReturnNull() {
        WebSocketConnection retrieved = sessionManager.getSession("non-existent");

        assertNull(retrieved);
    }

    @Test
    void getAllSessions_shouldReturnAllConnections() {
        WebSocketConnection conn1 = mock(WebSocketConnection.class);
        WebSocketConnection conn2 = mock(WebSocketConnection.class);
        WebSocketConnection conn3 = mock(WebSocketConnection.class);

        sessionManager.addSession("conn-1", conn1);
        sessionManager.addSession("conn-2", conn2);
        sessionManager.addSession("conn-3", conn3);

        Collection<WebSocketConnection> sessions = sessionManager.getAllSessions();

        assertEquals(3, sessions.size());
        assertTrue(sessions.contains(conn1));
        assertTrue(sessions.contains(conn2));
        assertTrue(sessions.contains(conn3));
    }

    @Test
    void getSessionCount_shouldReturnCorrectCount() {
        assertEquals(0, sessionManager.getSessionCount());

        sessionManager.addSession("conn-1", mock(WebSocketConnection.class));
        assertEquals(1, sessionManager.getSessionCount());

        sessionManager.addSession("conn-2", mock(WebSocketConnection.class));
        assertEquals(2, sessionManager.getSessionCount());

        sessionManager.removeSession("conn-1");
        assertEquals(1, sessionManager.getSessionCount());
    }

    @Test
    void hasSession_shouldReturnTrueForExistingSession() {
        sessionManager.addSession("conn-123", mock(WebSocketConnection.class));

        assertTrue(sessionManager.hasSession("conn-123"));
    }

    @Test
    void hasSession_shouldReturnFalseForNonExistentSession() {
        assertFalse(sessionManager.hasSession("non-existent"));
    }

    @Test
    void broadcast_shouldSendMessageToAllConnections() {
        WebSocketConnection conn1 = mock(WebSocketConnection.class);
        WebSocketConnection conn2 = mock(WebSocketConnection.class);
        WebSocketConnection conn3 = mock(WebSocketConnection.class);

        sessionManager.addSession("conn-1", conn1);
        sessionManager.addSession("conn-2", conn2);
        sessionManager.addSession("conn-3", conn3);

        String message = "{\"type\":\"TEST\",\"data\":{}}";
        sessionManager.broadcast(message);

        verify(conn1).sendTextAndAwait(message);
        verify(conn2).sendTextAndAwait(message);
        verify(conn3).sendTextAndAwait(message);
    }

    @Test
    void broadcastExcept_shouldSendMessageToAllExceptExcluded() {
        WebSocketConnection conn1 = mock(WebSocketConnection.class);
        WebSocketConnection conn2 = mock(WebSocketConnection.class);
        WebSocketConnection conn3 = mock(WebSocketConnection.class);

        sessionManager.addSession("conn-1", conn1);
        sessionManager.addSession("conn-2", conn2);
        sessionManager.addSession("conn-3", conn3);

        String message = "{\"type\":\"TEST\",\"data\":{}}";
        sessionManager.broadcastExcept(message, "conn-2");

        verify(conn1).sendTextAndAwait(message);
        verify(conn2, never()).sendTextAndAwait(message);
        verify(conn3).sendTextAndAwait(message);
    }

    @Test
    void clearAll_shouldRemoveAllSessions() {
        sessionManager.addSession("conn-1", mock(WebSocketConnection.class));
        sessionManager.addSession("conn-2", mock(WebSocketConnection.class));
        sessionManager.addSession("conn-3", mock(WebSocketConnection.class));

        assertEquals(3, sessionManager.getSessionCount());

        sessionManager.clearAll();

        assertEquals(0, sessionManager.getSessionCount());
        assertFalse(sessionManager.hasSession("conn-1"));
        assertFalse(sessionManager.hasSession("conn-2"));
        assertFalse(sessionManager.hasSession("conn-3"));
    }

    @Test
    void addSession_withSameId_shouldReplaceExistingSession() {
        WebSocketConnection oldConn = mock(WebSocketConnection.class);
        WebSocketConnection newConn = mock(WebSocketConnection.class);

        sessionManager.addSession("conn-123", oldConn);
        sessionManager.addSession("conn-123", newConn);

        assertEquals(1, sessionManager.getSessionCount());
        assertSame(newConn, sessionManager.getSession("conn-123"));
    }

    @Test
    void broadcast_withNoSessions_shouldNotThrowException() {
        assertDoesNotThrow(() -> sessionManager.broadcast("test message"));
    }
}
