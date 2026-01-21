package com.artistas.services;

import com.artistas.schemas.AlbumResponse;
import com.artistas.schemas.ArtistaResponse;
import com.artistas.services.events.AlbumCreatedEvent;
import com.artistas.websocket.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class NotificationServiceTest {

    @Inject
    NotificationService notificationService;

    @InjectMock
    SessionManager sessionManager;

    @Inject
    ObjectMapper objectMapper;

    @Test
    void onAlbumCreated_shouldBroadcastNewAlbumNotification() throws Exception {
        AlbumResponse albumResponse = createMockAlbumResponse(1L, "Test Album", List.of("Artist 1", "Artist 2"));
        AlbumCreatedEvent event = new AlbumCreatedEvent(albumResponse);

        notificationService.onAlbumCreated(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionManager).broadcast(messageCaptor.capture());

        String json = messageCaptor.getValue();
        assertTrue(json.contains("\"type\":\"NEW_ALBUM\""));
        assertTrue(json.contains("\"timestamp\":"));
        assertTrue(json.contains("\"data\":"));
    }

    @Test
    void onAlbumCreated_shouldIncludeCorrectAlbumData() throws Exception {
        AlbumResponse albumResponse = createMockAlbumResponse(42L, "My Album", List.of("Singer A", "Band B"));
        AlbumCreatedEvent event = new AlbumCreatedEvent(albumResponse);

        notificationService.onAlbumCreated(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionManager).broadcast(messageCaptor.capture());

        String json = messageCaptor.getValue();
        assertTrue(json.contains("\"id\":42"));
        assertTrue(json.contains("\"titulo\":\"My Album\""));
        assertTrue(json.contains("\"artistas\":[\"Singer A\",\"Band B\"]"));
    }

    @Test
    void onAlbumCreated_withNoArtists_shouldBroadcastEmptyArtistList() throws Exception {
        AlbumResponse albumResponse = createMockAlbumResponse(1L, "Solo Album", List.of());
        AlbumCreatedEvent event = new AlbumCreatedEvent(albumResponse);

        notificationService.onAlbumCreated(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionManager).broadcast(messageCaptor.capture());

        String json = messageCaptor.getValue();
        assertTrue(json.contains("\"artistas\":[]"));
    }

    @Test
    void onAlbumCreated_withNullArtists_shouldBroadcastEmptyArtistList() throws Exception {
        AlbumResponse albumResponse = new AlbumResponse();
        albumResponse.id = 1L;
        albumResponse.titulo = "Orphan Album";
        albumResponse.artistas = null;

        AlbumCreatedEvent event = new AlbumCreatedEvent(albumResponse);

        notificationService.onAlbumCreated(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionManager).broadcast(messageCaptor.capture());

        String json = messageCaptor.getValue();
        assertTrue(json.contains("\"artistas\":[]"));
    }

    @Test
    void onAlbumCreated_shouldIncludeTimestamp() throws Exception {
        AlbumResponse albumResponse = createMockAlbumResponse(1L, "Test", List.of());
        AlbumCreatedEvent event = new AlbumCreatedEvent(albumResponse);

        notificationService.onAlbumCreated(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionManager).broadcast(messageCaptor.capture());

        String json = messageCaptor.getValue();
        assertTrue(json.contains("\"timestamp\":\""));
    }

    @Test
    void onAlbumCreated_shouldProduceValidJson() throws Exception {
        AlbumResponse albumResponse = createMockAlbumResponse(1L, "Test Album", List.of("Artist"));
        AlbumCreatedEvent event = new AlbumCreatedEvent(albumResponse);

        notificationService.onAlbumCreated(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionManager).broadcast(messageCaptor.capture());

        String json = messageCaptor.getValue();
        assertDoesNotThrow(() -> objectMapper.readTree(json));
    }

    private AlbumResponse createMockAlbumResponse(Long id, String titulo, List<String> artistNames) {
        AlbumResponse response = new AlbumResponse();
        response.id = id;
        response.titulo = titulo;
        response.artistas = artistNames.stream().map(name -> {
            ArtistaResponse artista = new ArtistaResponse();
            artista.nome = name;
            return artista;
        }).toList();
        return response;
    }
}
