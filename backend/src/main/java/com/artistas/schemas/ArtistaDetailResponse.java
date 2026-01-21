package com.artistas.schemas;

import com.artistas.models.Artista;
import com.artistas.models.TipoArtista;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class ArtistaDetailResponse {

    public Long id;
    public String nome;
    public TipoArtista tipo;
    public Instant createdAt;
    public List<AlbumSummaryResponse> albuns;

    public static ArtistaDetailResponse of(Artista artista) {
        ArtistaDetailResponse response = new ArtistaDetailResponse();
        response.id = artista.id;
        response.nome = artista.nome;
        response.tipo = artista.tipo;
        response.createdAt = artista.createdAt;
        response.albuns = artista.albuns != null
            ? artista.albuns.stream().map(AlbumSummaryResponse::of).collect(Collectors.toList())
            : List.of();
        return response;
    }
}
