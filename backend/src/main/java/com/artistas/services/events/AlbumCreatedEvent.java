package com.artistas.services.events;

import com.artistas.schemas.AlbumResponse;

public class AlbumCreatedEvent {

    private final AlbumResponse album;

    public AlbumCreatedEvent(AlbumResponse album) {
        this.album = album;
    }

    public AlbumResponse getAlbum() {
        return album;
    }
}
