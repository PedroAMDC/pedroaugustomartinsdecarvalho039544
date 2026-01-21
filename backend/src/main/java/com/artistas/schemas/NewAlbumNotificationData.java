package com.artistas.schemas;

import java.util.List;

public class NewAlbumNotificationData {

    public Long id;
    public String titulo;
    public List<String> artistas;

    public static NewAlbumNotificationData of(AlbumResponse album) {
        NewAlbumNotificationData data = new NewAlbumNotificationData();
        data.id = album.id;
        data.titulo = album.titulo;
        data.artistas = album.artistas != null
            ? album.artistas.stream()
                .map(artista -> artista.nome)
                .toList()
            : List.of();
        return data;
    }
}
