package com.artistas.schemas;

import com.artistas.models.Album;
import java.time.Instant;

public class AlbumSummaryResponse {

    public Long id;
    public String titulo;
    public Integer anoLancamento;
    public Instant createdAt;

    public static AlbumSummaryResponse of(Album album) {
        AlbumSummaryResponse response = new AlbumSummaryResponse();
        response.id = album.id;
        response.titulo = album.titulo;
        response.anoLancamento = album.anoLancamento;
        response.createdAt = album.createdAt;
        return response;
    }
}
