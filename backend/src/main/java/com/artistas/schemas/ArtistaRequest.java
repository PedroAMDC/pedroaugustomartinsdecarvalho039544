package com.artistas.schemas;

import com.artistas.models.TipoArtista;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ArtistaRequest {

    @NotBlank
    public String nome;

    @NotNull
    public TipoArtista tipo;
}
