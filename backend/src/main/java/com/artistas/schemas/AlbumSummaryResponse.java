package com.artistas.schemas;

import com.artistas.models.Album;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Album summary (used in artist detail)")
public class AlbumSummaryResponse {

    @Schema(description = "Album unique identifier", examples = {"1"})
    public Long id;

    @Schema(description = "Album title", examples = {"Dois"})
    public String titulo;

    @Schema(description = "Release year", examples = {"1986"})
    public Integer anoLancamento;

    @Schema(description = "Creation timestamp", examples = {"2026-01-15T10:30:00Z"})
    public Instant createdAt;

    @Schema(description = "Presigned URL for the primary cover image", nullable = true)
    public String capaUrl;

    public static AlbumSummaryResponse of(Album album) {
        return of(album, null);
    }

    public static AlbumSummaryResponse of(Album album, String capaUrl) {
        AlbumSummaryResponse response = new AlbumSummaryResponse();
        response.id = album.id;
        response.titulo = album.titulo;
        response.anoLancamento = album.anoLancamento;
        response.createdAt = album.createdAt;
        response.capaUrl = capaUrl;
        return response;
    }
}
