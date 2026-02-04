package com.artistas.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "albuns")
public class Album extends PanacheEntity {

    @NotBlank
    @Column(nullable = false)
    public String titulo;

    @NotNull
    @Min(1900)
    @Max(2100)
    @Column(name = "ano_lancamento", nullable = false)
    public Integer anoLancamento;

    @ManyToMany(mappedBy = "albuns")
    public List<Artista> artistas = new ArrayList<>();

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CapaAlbum> capas = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public static Album create(String titulo, Integer anoLancamento) {
        Album album = new Album();
        album.titulo = titulo;
        album.anoLancamento = anoLancamento;
        return album;
    }

    public static Album findByTitulo(String titulo) {
        return find("titulo", titulo).firstResult();
    }
}
