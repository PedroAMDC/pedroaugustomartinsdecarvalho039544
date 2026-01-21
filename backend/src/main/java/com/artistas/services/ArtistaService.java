package com.artistas.services;

import com.artistas.models.Artista;
import com.artistas.models.TipoArtista;
import com.artistas.schemas.ArtistaDetailResponse;
import com.artistas.schemas.ArtistaListResponse;
import com.artistas.schemas.ArtistaRequest;
import com.artistas.schemas.ArtistaResponse;
import com.artistas.services.exceptions.NotFoundException;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ArtistaService {

    public ArtistaListResponse list(Integer page, Integer size, String nome, TipoArtista tipo, String sortDirection) {
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (nome != null && !nome.isBlank()) {
            query.append(" and lower(nome) like lower(:nome)");
            params.put("nome", "%" + nome + "%");
        }

        if (tipo != null) {
            query.append(" and tipo = :tipo");
            params.put("tipo", tipo);
        }

        Sort sort = Sort.by("nome");
        if ("desc".equalsIgnoreCase(sortDirection)) {
            sort = sort.descending();
        }

        PanacheQuery<Artista> panacheQuery = Artista.find(query.toString(), sort, params);
        long total = panacheQuery.count();
        List<Artista> artistas = panacheQuery.page(Page.of(page, size)).list();

        List<ArtistaResponse> content = artistas.stream()
            .map(ArtistaResponse::of)
            .toList();

        return ArtistaListResponse.of(content, page, size, total);
    }

    public ArtistaResponse findById(Long id) {
        Artista artista = Artista.findById(id);
        if (artista == null) {
            throw new NotFoundException("Artist not found");
        }
        return ArtistaResponse.of(artista);
    }

    public ArtistaDetailResponse findByIdWithAlbuns(Long id) {
        Artista artista = Artista.findById(id);
        if (artista == null) {
            throw new NotFoundException("Artist not found");
        }
        return ArtistaDetailResponse.of(artista);
    }

    @Transactional
    public ArtistaResponse create(ArtistaRequest request) {
        Artista artista = Artista.create(request.nome, request.tipo);
        artista.persist();
        return ArtistaResponse.of(artista);
    }

    @Transactional
    public ArtistaResponse update(Long id, ArtistaRequest request) {
        Artista artista = Artista.findById(id);
        if (artista == null) {
            throw new NotFoundException("Artist not found");
        }
        artista.nome = request.nome;
        artista.tipo = request.tipo;
        return ArtistaResponse.of(artista);
    }
}
