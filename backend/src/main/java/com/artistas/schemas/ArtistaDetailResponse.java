package com.artistas.schemas;

import com.artistas.models.Artista;
import com.artistas.models.TipoArtista;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Artist with associated albums")
public class ArtistaDetailResponse {

    @Schema(description = "Artist unique identifier", examples = {"1"})
    public Long id;

    @Schema(description = "Artist name", examples = {"Legiao Urbana"})
    public String nome;

    @Schema(description = "Artist type", examples = {"BANDA"})
    public TipoArtista tipo;

    @Schema(description = "Creation timestamp", examples = {"2026-01-15T10:30:00Z"})
    public Instant createdAt;

    @Schema(description = "List of artist albums")
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

    public static ArtistaDetailResponse of(Artista artista, List<AlbumSummaryResponse> albuns) {
        ArtistaDetailResponse response = new ArtistaDetailResponse();
        response.id = artista.id;
        response.nome = artista.nome;
        response.tipo = artista.tipo;
        response.createdAt = artista.createdAt;
        response.albuns = albuns;
        return response;
    }
}
