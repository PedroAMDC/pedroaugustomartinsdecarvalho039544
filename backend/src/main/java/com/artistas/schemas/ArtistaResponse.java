package com.artistas.schemas;

import com.artistas.models.Artista;
import com.artistas.models.TipoArtista;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Artist summary data")
public class ArtistaResponse {

    @Schema(description = "Artist unique identifier", examples = {"1"})
    public Long id;

    @Schema(description = "Artist name", examples = {"Legiao Urbana"})
    public String nome;

    @Schema(description = "Artist type", examples = {"BANDA"})
    public TipoArtista tipo;

    @Schema(description = "Number of associated albums", examples = {"5"})
    public Integer quantidadeAlbuns;

    @Schema(description = "Creation timestamp", examples = {"2026-01-15T10:30:00Z"})
    public Instant createdAt;

    public static ArtistaResponse of(Artista artista) {
        ArtistaResponse response = new ArtistaResponse();
        response.id = artista.id;
        response.nome = artista.nome;
        response.tipo = artista.tipo;
        response.quantidadeAlbuns = artista.albuns != null ? artista.albuns.size() : 0;
        response.createdAt = artista.createdAt;
        return response;
    }
}
