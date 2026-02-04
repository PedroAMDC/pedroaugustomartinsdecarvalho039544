package com.artistas.services;

import com.artistas.models.Regional;
import com.artistas.schemas.RegionalListResponse;
import com.artistas.schemas.RegionalResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RegionalService {

    public RegionalListResponse list(Boolean ativo) {
        List<Regional> regionais;

        if (ativo == null) {
            regionais = Regional.listAll();
        } else if (ativo) {
            regionais = Regional.findAllAtivos();
        } else {
            regionais = Regional.list("ativo", false);
        }

        List<RegionalResponse> content = regionais.stream()
            .map(RegionalResponse::of)
            .toList();

        return RegionalListResponse.of(content);
    }
}
