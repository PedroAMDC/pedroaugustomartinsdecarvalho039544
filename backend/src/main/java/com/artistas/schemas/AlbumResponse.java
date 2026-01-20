package com.artistas.schemas;

import com.artistas.models.Album;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class AlbumResponse {

    public Long id;
    public String titulo;
    public Integer anoLancamento;
    public List<ArtistaResponse> artistas;
    public List<CapaAlbumResponse> capas;
    public Instant createdAt;

    public static AlbumResponse of(Album album) {
        AlbumResponse response = new AlbumResponse();
        response.id = album.id;
        response.titulo = album.titulo;
        response.anoLancamento = album.anoLancamento;
        response.artistas = album.artistas != null
            ? album.artistas.stream().map(ArtistaResponse::of).collect(Collectors.toList())
            : List.of();
        response.capas = album.capas != null
            ? album.capas.stream().map(CapaAlbumResponse::of).collect(Collectors.toList())
            : List.of();
        response.createdAt = album.createdAt;
        return response;
    }
}
