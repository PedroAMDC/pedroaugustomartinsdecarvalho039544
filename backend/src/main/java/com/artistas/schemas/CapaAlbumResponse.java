package com.artistas.schemas;

import com.artistas.models.CapaAlbum;
import java.time.Instant;

public class CapaAlbumResponse {

    public Long id;
    public String minioKey;
    public String originalName;
    public String contentType;
    public Long tamanhoBytes;
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
