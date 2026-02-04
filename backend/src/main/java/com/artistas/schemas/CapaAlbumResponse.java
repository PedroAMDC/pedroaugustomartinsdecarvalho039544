package com.artistas.schemas;

import com.artistas.models.CapaAlbum;
import java.time.Instant;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Album cover image data")
public class CapaAlbumResponse {

    @Schema(description = "Cover unique identifier", examples = {"1"})
    public Long id;

    @Schema(description = "MinIO object key", examples = {"capas/1/uuid-filename.jpg"})
    public String minioKey;

    @Schema(description = "Original file name", examples = {"album-cover.jpg"})
    public String originalName;

    @Schema(description = "MIME content type", examples = {"image/jpeg"})
    public String contentType;

    @Schema(description = "File size in bytes", examples = {"204800"})
    public Long tamanhoBytes;

    @Schema(description = "Upload timestamp", examples = {"2026-01-15T10:30:00Z"})
    public Instant createdAt;

    public static CapaAlbumResponse of(CapaAlbum capa) {
        CapaAlbumResponse response = new CapaAlbumResponse();
        response.id = capa.id;
        response.minioKey = capa.minioKey;
        response.originalName = capa.originalName;
        response.contentType = capa.contentType;
        response.tamanhoBytes = capa.tamanhoBytes;
        response.createdAt = capa.createdAt;
        return response;
    }
}
