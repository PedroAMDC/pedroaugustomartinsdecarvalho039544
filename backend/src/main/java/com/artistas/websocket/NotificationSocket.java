package com.artistas.websocket;

import com.artistas.services.TokenService;
import com.artistas.services.exceptions.InvalidTokenException;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.quarkus.websockets.next.HandshakeRequest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@WebSocket(path = "/ws/notifications")
public class NotificationSocket {

    @Inject
    TokenService tokenService;

    @Inject
    WebSocketConnection connection;

    @OnOpen
    public void onOpen(HandshakeRequest handshake) {
        String token = extractToken(handshake);

        if (token == null || token.isBlank()) {
            Log.warn("WebSocket connection attempt without token");
            connection.closeAndAwait();
            return;
        }

        try {
            JsonWebToken jwt = tokenService.validateAccessToken(token);
            String userId = jwt.getSubject();
            Log.infof("WebSocket connection opened for user %s, connection ID: %s", userId, connection.id());
        } catch (InvalidTokenException e) {
            Log.warnf("WebSocket connection attempt with invalid token: %s", e.getMessage());
            connection.closeAndAwait();
        }
    }

    @OnClose
    public void onClose() {
        Log.infof("WebSocket connection closed, connection ID: %s", connection.id());
    }

    @OnError
    public void onError(Throwable error) {
        Log.errorf(error, "WebSocket error for connection ID: %s", connection.id());
    }

    @OnTextMessage
    public String onMessage(String message) {
        if ("ping".equalsIgnoreCase(message)) {
            return "pong";
        }
        return null;
    }

    private String extractToken(HandshakeRequest handshake) {
        String token = extractQueryParam(handshake.query(), "token");

        if (token == null || token.isBlank()) {
            String authHeader = handshake.header("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        return token;
    }

    private String extractQueryParam(String queryString, String paramName) {
        if (queryString == null || queryString.isBlank()) {
            return null;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
    }
}
