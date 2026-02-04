package com.artistas.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artistas")
public class Artista extends PanacheEntity {

    @NotBlank
    @Column(nullable = false)
    public String nome;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TipoArtista tipo;

    @ManyToMany
    @JoinTable(
        name = "artista_album",
        joinColumns = @JoinColumn(name = "artista_id"),
        inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    public List<Album> albuns = new ArrayList<>();

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

    public static Artista create(String nome, TipoArtista tipo) {
        Artista artista = new Artista();
        artista.nome = nome;
        artista.tipo = tipo;
        return artista;
    }

    public static Artista findByNome(String nome) {
        return find("nome", nome).firstResult();
    }

    public static List<Artista> findByTipo(TipoArtista tipo) {
        return list("tipo", tipo);
    }
}
