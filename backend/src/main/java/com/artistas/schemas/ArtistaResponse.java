package com.artistas.schemas;

import com.artistas.models.Artista;
import com.artistas.models.TipoArtista;
import java.time.Instant;

public class ArtistaResponse {

    public Long id;
    public String nome;
    public TipoArtista tipo;
    public Integer quantidadeAlbuns;
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
