package com.artistas.websocket;

import com.artistas.models.Usuario;
import com.artistas.services.TokenService;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class NotificationSocketTest {

    @TestHTTPResource("/ws/notifications")
    URI wsUri;

    @Inject
    TokenService tokenService;

    @Inject
    SessionManager sessionManager;

    private static final String TEST_EMAIL = "ws-test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "WebSocket Test User";

    private Usuario testUsuario;
    private String validToken;

    @BeforeEach
    @Transactional
    void setUp() {
        sessionManager.clearAll();
        Usuario.delete("email", TEST_EMAIL);
        testUsuario = Usuario.create(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
        testUsuario.persist();
        validToken = tokenService.generateAccessToken(testUsuario);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        sessionManager.clearAll();
        Usuario.delete("email", TEST_EMAIL);
    }

    @Test
    void connect_withValidToken_shouldEstablishConnection() throws Exception {
        URI uri = URI.create(wsUri.toString() + "?token=" + validToken);
        TestWebSocketClient client = new TestWebSocketClient();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = container.connectToServer(client, uri);

        assertTrue(client.awaitOpen(5, TimeUnit.SECONDS));
        assertTrue(session.isOpen());

        session.close();
        assertTrue(client.awaitClose(5, TimeUnit.SECONDS));
    }

    @Test
    void connect_withoutToken_shouldCloseConnection() throws Exception {
        TestWebSocketClient client = new TestWebSocketClient();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        try {
            Session session = container.connectToServer(client, wsUri);
            assertTrue(client.awaitClose(5, TimeUnit.SECONDS));
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    void connect_withInvalidToken_shouldCloseConnection() throws Exception {
        URI uri = URI.create(wsUri.toString() + "?token=invalid.token.here");
        TestWebSocketClient client = new TestWebSocketClient();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        try {
            Session session = container.connectToServer(client, uri);
            assertTrue(client.awaitClose(5, TimeUnit.SECONDS));
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    void sendPing_shouldReceivePong() throws Exception {
        URI uri = URI.create(wsUri.toString() + "?token=" + validToken);
        TestWebSocketClient client = new TestWebSocketClient();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = container.connectToServer(client, uri);

        assertTrue(client.awaitOpen(5, TimeUnit.SECONDS));

        session.getBasicRemote().sendText("ping");
        String response = client.awaitMessage(5, TimeUnit.SECONDS);

        assertEquals("pong", response);

        session.close();
    }

    @Test
    void connect_shouldRegisterSessionInManager() throws Exception {
        URI uri = URI.create(wsUri.toString() + "?token=" + validToken);
        TestWebSocketClient client = new TestWebSocketClient();

        assertEquals(0, sessionManager.getSessionCount());

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = container.connectToServer(client, uri);

        assertTrue(client.awaitOpen(5, TimeUnit.SECONDS));
        Thread.sleep(100);

        assertTrue(sessionManager.getSessionCount() > 0);

        session.close();
    }

    @Test
    void disconnect_shouldRemoveSessionFromManager() throws Exception {
        URI uri = URI.create(wsUri.toString() + "?token=" + validToken);
        TestWebSocketClient client = new TestWebSocketClient();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = container.connectToServer(client, uri);

        assertTrue(client.awaitOpen(5, TimeUnit.SECONDS));
        Thread.sleep(100);

        int countBefore = sessionManager.getSessionCount();
        assertTrue(countBefore > 0);

        session.close();
        assertTrue(client.awaitClose(5, TimeUnit.SECONDS));
        Thread.sleep(100);

        assertEquals(countBefore - 1, sessionManager.getSessionCount());
    }

    @ClientEndpoint
    public static class TestWebSocketClient {

        private final LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<>();
        private final CountDownLatch openLatch = new CountDownLatch(1);
        private final CountDownLatch closeLatch = new CountDownLatch(1);

        @OnOpen
        public void onOpen(Session session) {
            openLatch.countDown();
        }

        @OnMessage
        public void onMessage(String message) {
            messages.add(message);
        }

        @OnClose
        public void onClose() {
            closeLatch.countDown();
        }

        @OnError
        public void onError(Throwable error) {
            closeLatch.countDown();
        }

        public boolean awaitOpen(long timeout, TimeUnit unit) throws InterruptedException {
            return openLatch.await(timeout, unit);
        }

        public boolean awaitClose(long timeout, TimeUnit unit) throws InterruptedException {
            return closeLatch.await(timeout, unit);
        }

        public String awaitMessage(long timeout, TimeUnit unit) throws InterruptedException {
            return messages.poll(timeout, unit);
        }
    }
}
