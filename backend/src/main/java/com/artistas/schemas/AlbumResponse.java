package com.artistas.schemas;

import com.artistas.models.Album;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Album with artists and covers")
public class AlbumResponse {

    @Schema(description = "Album unique identifier", examples = {"1"})
    public Long id;

    @Schema(description = "Album title", examples = {"Dois"})
    public String titulo;

    @Schema(description = "Release year", examples = {"1986"})
    public Integer anoLancamento;

    @Schema(description = "List of associated artists")
    public List<ArtistaResponse> artistas;

    @Schema(description = "List of album covers")
    public List<CapaAlbumResponse> capas;

    @Schema(description = "Creation timestamp", examples = {"2026-01-15T10:30:00Z"})
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
