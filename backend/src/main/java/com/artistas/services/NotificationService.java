package com.artistas.services;

import com.artistas.schemas.NewAlbumNotificationData;
import com.artistas.schemas.WebSocketMessage;
import com.artistas.services.events.AlbumCreatedEvent;
import com.artistas.websocket.SessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class NotificationService {

    private static final String NEW_ALBUM_TYPE = "NEW_ALBUM";

    @Inject
    SessionManager sessionManager;

    @Inject
    ObjectMapper objectMapper;

    public void onAlbumCreated(@Observes AlbumCreatedEvent event) {
        NewAlbumNotificationData data = NewAlbumNotificationData.of(event.getAlbum());
        WebSocketMessage<NewAlbumNotificationData> message = WebSocketMessage.of(NEW_ALBUM_TYPE, data);

        try {
            String json = objectMapper.writeValueAsString(message);
            sessionManager.broadcast(json);
            Log.infof("Broadcast NEW_ALBUM notification for album ID: %d", data.id);
        } catch (JsonProcessingException e) {
            Log.errorf(e, "Failed to serialize notification for album ID: %d", data.id);
        }
    }
}
