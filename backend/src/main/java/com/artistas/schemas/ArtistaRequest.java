package com.artistas.schemas;

import com.artistas.models.TipoArtista;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Artist creation/update request")
public class ArtistaRequest {

    @NotBlank
    @Schema(description = "Artist name", examples = {"Legiao Urbana"})
    public String nome;

    @NotNull
    @Schema(description = "Artist type", examples = {"BANDA"})
    public TipoArtista tipo;
}
