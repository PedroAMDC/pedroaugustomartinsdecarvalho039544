package com.artistas.schemas;

import com.artistas.models.Regional;

public class RegionalResponse {

    public Integer id;
    public String nome;
    public Boolean ativo;

    public static RegionalResponse of(Regional regional) {
        RegionalResponse response = new RegionalResponse();
        response.id = regional.id;
        response.nome = regional.nome;
        response.ativo = regional.ativo;
        return response;
    }
}
