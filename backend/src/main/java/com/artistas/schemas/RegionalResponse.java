package com.artistas.schemas;

import com.artistas.models.Regional;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Regional reference data")
public class RegionalResponse {

    @Schema(description = "Regional external identifier", examples = {"1"})
    public Integer id;

    @Schema(description = "Regional name", examples = {"Norte"})
    public String nome;

    @Schema(description = "Whether the regional is active", examples = {"true"})
    public Boolean ativo;

    public static RegionalResponse of(Regional regional) {
        RegionalResponse response = new RegionalResponse();
        response.id = regional.externalId;
        response.nome = regional.nome;
        response.ativo = regional.ativo;
        return response;
    }
}
