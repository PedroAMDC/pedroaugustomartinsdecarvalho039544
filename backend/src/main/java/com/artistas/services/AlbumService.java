package com.artistas.services;

import com.artistas.models.Album;
import com.artistas.models.Artista;
import com.artistas.models.TipoArtista;
import com.artistas.schemas.AlbumPaginatedResponse;
import com.artistas.schemas.AlbumRequest;
import com.artistas.schemas.AlbumResponse;
import com.artistas.services.events.AlbumCreatedEvent;
import com.artistas.services.exceptions.NotFoundException;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AlbumService {

    @Inject
    Event<AlbumCreatedEvent> albumCreatedEvent;

    public AlbumPaginatedResponse list(Integer page, Integer size, Long artistaId, TipoArtista tipoArtista, String sortDirection) {
        Sort sort = Sort.by("titulo");
        if ("desc".equalsIgnoreCase(sortDirection)) {
            sort = sort.descending();
        }

        PanacheQuery<Album> panacheQuery;

        if (artistaId != null && tipoArtista != null) {
            panacheQuery = Album.find(
                "select distinct a from Album a join a.artistas art where art.id = ?1 and art.tipo = ?2",
                sort, artistaId, tipoArtista
            );
        } else if (artistaId != null) {
            panacheQuery = Album.find(
                "select distinct a from Album a join a.artistas art where art.id = ?1",
                sort, artistaId
            );
        } else if (tipoArtista != null) {
            panacheQuery = Album.find(
                "select distinct a from Album a join a.artistas art where art.tipo = ?1",
                sort, tipoArtista
            );
        } else {
            panacheQuery = Album.findAll(sort);
        }

        long total = panacheQuery.count();
        List<Album> albums = panacheQuery.page(Page.of(page, size)).list();

        List<AlbumResponse> content = albums.stream()
            .map(AlbumResponse::of)
            .toList();

        return AlbumPaginatedResponse.of(content, page, size, total);
    }

    public AlbumResponse findById(Long id) {
        Album album = Album.findById(id);
        if (album == null) {
            throw new NotFoundException("Album not found");
        }
        return AlbumResponse.of(album);
    }

    @Transactional
    public AlbumResponse create(AlbumRequest request) {
        Album album = Album.create(request.titulo, request.anoLancamento);

        for (Long artistaId : request.artistaIds) {
            Artista artista = Artista.findById(artistaId);
            if (artista == null) {
                throw new NotFoundException("Artist with id " + artistaId + " not found");
            }
            artista.albuns.add(album);
            album.artistas.add(artista);
        }

        album.persist();
        AlbumResponse response = AlbumResponse.of(album);
        albumCreatedEvent.fire(new AlbumCreatedEvent(response));
        return response;
    }

    @Transactional
    public AlbumResponse update(Long id, AlbumRequest request) {
        Album album = Album.findById(id);
        if (album == null) {
            throw new NotFoundException("Album not found");
        }

        album.titulo = request.titulo;
        album.anoLancamento = request.anoLancamento;

        for (Artista artista : new ArrayList<>(album.artistas)) {
            artista.albuns.remove(album);
        }
        album.artistas.clear();

        for (Long artistaId : request.artistaIds) {
            Artista artista = Artista.findById(artistaId);
            if (artista == null) {
                throw new NotFoundException("Artist with id " + artistaId + " not found");
            }
            artista.albuns.add(album);
            album.artistas.add(artista);
        }

        return AlbumResponse.of(album);
    }
}
