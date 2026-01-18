package com.artistas.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "capas_album")
public class CapaAlbum extends PanacheEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    public Album album;

    @NotBlank
    @Column(name = "minio_key", nullable = false, length = 500)
    public String minioKey;

    @NotBlank
    @Column(name = "nome_original", nullable = false)
    public String originalName;

    @NotBlank
    @Column(name = "content_type", nullable = false, length = 100)
    public String contentType;

    @NotNull
    @Column(name = "tamanho_bytes", nullable = false)
    public Long tamanhoBytes;

    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }

    public static CapaAlbum create(Album album, String minioKey, String originalName, String contentType, Long tamanhoBytes) {
        CapaAlbum capa = new CapaAlbum();
        capa.album = album;
        capa.minioKey = minioKey;
        capa.originalName = originalName;
        capa.contentType = contentType;
        capa.tamanhoBytes = tamanhoBytes;
        return capa;
    }
}
