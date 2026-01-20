package com.artistas.services;

import com.artistas.models.Album;
import com.artistas.models.Artista;
import com.artistas.models.TipoArtista;
import com.artistas.schemas.AlbumPaginatedResponse;
import com.artistas.schemas.AlbumRequest;
import com.artistas.schemas.AlbumResponse;
import com.artistas.services.exceptions.NotFoundException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class AlbumServiceTest {

    @Inject
    AlbumService albumService;

    private static final String TEST_ALBUM_1 = "Test Album Alpha";
    private static final String TEST_ALBUM_2 = "Test Album Beta";
    private static final String TEST_ALBUM_3 = "Test Album Gamma";
    private static final String TEST_ARTISTA_1 = "Test Artist One";
    private static final String TEST_ARTISTA_2 = "Test Artist Two";

    @BeforeEach
    @Transactional
    void setUp() {
        Album.delete("titulo like ?1", "Test Album%");
        Artista.delete("nome like ?1", "Test Artist%");
    }

    @AfterEach
    @Transactional
    void tearDown() {
        Album.delete("titulo like ?1", "Test Album%");
        Artista.delete("nome like ?1", "Test Artist%");
    }

    @Test
    @Transactional
    void list_withPagination_shouldReturnPaginatedResults() {
        Artista artista = Artista.create(TEST_ARTISTA_1, TipoArtista.CANTOR);
        artista.persist();

        Album album1 = Album.create(TEST_ALBUM_1, 2020);
        album1.artistas.add(artista);
        artista.albuns.add(album1);
        album1.persist();

        Album album2 = Album.create(TEST_ALBUM_2, 2021);
        album2.artistas.add(artista);
        artista.albuns.add(album2);
        album2.persist();

        Album album3 = Album.create(TEST_ALBUM_3, 2022);
        album3.artistas.add(artista);
        artista.albuns.add(album3);
        album3.persist();

        AlbumPaginatedResponse response = albumService.list(0, 2, null, null, null);

        assertNotNull(response);
        assertEquals(2, response.content.size());
        assertEquals(0, response.page);
        assertEquals(2, response.size);
        assertTrue(response.totalElements >= 3);
    }

    @Test
    @Transactional
    void list_withArtistaIdFilter_shouldReturnAlbumsOfThatArtist() {
        Artista artista1 = Artista.create(TEST_ARTISTA_1, TipoArtista.CANTOR);
        artista1.persist();

        Artista artista2 = Artista.create(TEST_ARTISTA_2, TipoArtista.BANDA);
        artista2.persist();

        Album album1 = Album.create(TEST_ALBUM_1, 2020);
        album1.artistas.add(artista1);
        artista1.albuns.add(album1);
        album1.persist();

        Album album2 = Album.create(TEST_ALBUM_2, 2021);
        album2.artistas.add(artista2);
        artista2.albuns.add(album2);
        album2.persist();

        AlbumPaginatedResponse response = albumService.list(0, 10, artista1.id, null, null);

        assertNotNull(response);
        assertEquals(1, response.content.size());
        assertEquals(TEST_ALBUM_1, response.content.get(0).titulo);
    }

    @Test
    @Transactional
    void list_withTipoArtistaFilter_shouldReturnAlbumsOfArtistType() {
        Artista artista1 = Artista.create(TEST_ARTISTA_1, TipoArtista.CANTOR);
        artista1.persist();

        Artista artista2 = Artista.create(TEST_ARTISTA_2, TipoArtista.BANDA);
        artista2.persist();

        Album album1 = Album.create(TEST_ALBUM_1, 2020);
        album1.artistas.add(artista1);
        artista1.albuns.add(album1);
        album1.persist();

        Album album2 = Album.create(TEST_ALBUM_2, 2021);
        album2.artistas.add(artista2);
        artista2.albuns.add(album2);
        album2.persist();

        AlbumPaginatedResponse response = albumService.list(0, 10, null, TipoArtista.CANTOR, null);

        assertNotNull(response);
        assertEquals(1, response.content.size());
        assertEquals(TEST_ALBUM_1, response.content.get(0).titulo);
    }

    @Test
    @Transactional
    void list_withCombinedFilters_shouldApplyAllFilters() {
        Artista artista1 = Artista.create(TEST_ARTISTA_1, TipoArtista.CANTOR);
        artista1.persist();

        Artista artista2 = Artista.create(TEST_ARTISTA_2, TipoArtista.CANTOR);
        artista2.persist();

        Album album1 = Album.create(TEST_ALBUM_1, 2020);
        album1.artistas.add(artista1);
        artista1.albuns.add(album1);
        album1.persist();

        Album album2 = Album.create(TEST_ALBUM_2, 2021);
        album2.artistas.add(artista2);
        artista2.albuns.add(album2);
        album2.persist();

        AlbumPaginatedResponse response = albumService.list(0, 10, artista1.id, TipoArtista.CANTOR, null);

        assertNotNull(response);
        assertEquals(1, response.content.size());
        assertEquals(TEST_ALBUM_1, response.content.get(0).titulo);
    }

    @Test
    @Transactional
    void list_withSortAsc_shouldReturnSortedByTitulo() {
        Artista artista = Artista.create(TEST_ARTISTA_1, TipoArtista.CANTOR);
        artista.persist();

        Album album3 = Album.create(TEST_ALBUM_3, 2022);
        album3.artistas.add(artista);
        artista.albuns.add(album3);
        album3.persist();

        Album album1 = Album.create(TEST_ALBUM_1, 2020);
        album1.artistas.add(artista);
        artista.albuns.add(album1);
        album1.persist();

        Album album2 = Album.create(TEST_ALBUM_2, 2021);
        album2.artistas.add(artista);
        artista.albuns.add(album2);
        album2.persist();

        AlbumPaginatedResponse response = albumService.list(0, 10, null, null, "asc");

        assertNotNull(response);
        assertTrue(response.content.size() >= 3);
        assertEquals(TEST_ALBUM_1, response.content.get(0).titulo);
        assertEquals(TEST_ALBUM_2, response.content.get(1).titulo);
        assertEquals(TEST_ALBUM_3, response.content.get(2).titulo);
    }

    @Test
    @Transactional
    void list_withSortDesc_shouldReturnSortedDescending() {
        Artista artista = Artista.create(TEST_ARTISTA_1, TipoArtista.CANTOR);
        artista.persist();

        Album album1 = Album.create(TEST_ALBUM_1, 2020);
        album1.artistas.add(artista);
        artista.albuns.add(album1);
        album1.persist();

        Album album2 = Album.create(TEST_ALBUM_2, 2021);
        album2.artistas.add(artista);
        artista.albuns.add(album2);
        album2.persist();

        Album album3 = Album.create(TEST_ALBUM_3, 2022);
        album3.artistas.add(artista);
        artista.albuns.add(album3);
        album3.persist();

        AlbumPaginatedResponse response = albumService.list(0, 10, null, null, "desc");

        assertNotNull(response);
        assertTrue(response.content.size() >= 3);
        assertEquals(TEST_ALBUM_3, response.content.get(0).titulo);
        assertEquals(TEST_ALBUM_2, response.content.get(1).titulo);
        assertEquals(TEST_ALBUM_1, response.content.get(2).titulo);
    }

    @Test
    @Transactional
    void findById_withExistingId_shouldReturnAlbumWithArtistasAndCapas() {
        Artista artista = Artista.create(TEST_ARTISTA_1, TipoArtista.CANTOR);
        artista.persist();

        Album album = Album.create(TEST_ALBUM_1, 2020);
        album.artistas.add(artista);
        artista.albuns.add(album);
        album.persist();

        AlbumResponse response = albumService.findById(album.id);

        assertNotNull(response);
        assertEquals(album.id, response.id);
        assertEquals(TEST_ALBUM_1, response.titulo);
        assertEquals(2020, response.anoLancamento);
        assertNotNull(response.artistas);
        assertEquals(1, response.artistas.size());
        assertEquals(TEST_ARTISTA_1, response.artistas.get(0).nome);
    }

    @Test
    void findById_withNonExistingId_shouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> albumService.findById(999999L)
        );

        assertEquals("Album not found", exception.getMessage());
    }

    @Test
    void create_withValidRequest_shouldCreateAlbumWithArtistas() {
        Artista artista = createAndPersistArtista(TEST_ARTISTA_1, TipoArtista.CANTOR);

        AlbumRequest request = new AlbumRequest();
        request.titulo = TEST_ALBUM_1;
        request.anoLancamento = 2020;
        request.artistaIds = List.of(artista.id);

        AlbumResponse response = albumService.create(request);

        assertNotNull(response);
        assertNotNull(response.id);
        assertEquals(TEST_ALBUM_1, response.titulo);
        assertEquals(2020, response.anoLancamento);
        assertNotNull(response.artistas);
        assertEquals(1, response.artistas.size());
        assertEquals(TEST_ARTISTA_1, response.artistas.get(0).nome);
    }

    @Test
    void create_withNonExistingArtistId_shouldThrowNotFoundException() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = TEST_ALBUM_1;
        request.anoLancamento = 2020;
        request.artistaIds = List.of(999999L);

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> albumService.create(request)
        );

        assertTrue(exception.getMessage().contains("Artist with id"));
    }

    @Test
    @Transactional
    void update_withValidRequest_shouldUpdateAlbumFields() {
        Artista artista = Artista.create(TEST_ARTISTA_1, TipoArtista.CANTOR);
        artista.persist();

        Album album = Album.create(TEST_ALBUM_1, 2020);
        album.artistas.add(artista);
        artista.albuns.add(album);
        album.persist();

        AlbumRequest request = new AlbumRequest();
        request.titulo = TEST_ALBUM_2;
        request.anoLancamento = 2021;
        request.artistaIds = List.of(artista.id);

        AlbumResponse response = albumService.update(album.id, request);

        assertNotNull(response);
        assertEquals(album.id, response.id);
        assertEquals(TEST_ALBUM_2, response.titulo);
        assertEquals(2021, response.anoLancamento);
    }

    @Test
    @Transactional
    void update_withNewArtistas_shouldReplaceArtistas() {
        Artista artista1 = Artista.create(TEST_ARTISTA_1, TipoArtista.CANTOR);
        artista1.persist();

        Artista artista2 = Artista.create(TEST_ARTISTA_2, TipoArtista.BANDA);
        artista2.persist();

        Album album = Album.create(TEST_ALBUM_1, 2020);
        album.artistas.add(artista1);
        artista1.albuns.add(album);
        album.persist();

        AlbumRequest request = new AlbumRequest();
        request.titulo = TEST_ALBUM_1;
        request.anoLancamento = 2020;
        request.artistaIds = List.of(artista2.id);

        AlbumResponse response = albumService.update(album.id, request);

        assertNotNull(response);
        assertEquals(1, response.artistas.size());
        assertEquals(TEST_ARTISTA_2, response.artistas.get(0).nome);
    }

    @Test
    void update_withNonExistingAlbumId_shouldThrowNotFoundException() {
        Artista artista = createAndPersistArtista(TEST_ARTISTA_1, TipoArtista.CANTOR);

        AlbumRequest request = new AlbumRequest();
        request.titulo = TEST_ALBUM_1;
        request.anoLancamento = 2020;
        request.artistaIds = List.of(artista.id);

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> albumService.update(999999L, request)
        );

        assertEquals("Album not found", exception.getMessage());
    }

    @Test
    @Transactional
    void update_withNonExistingArtistId_shouldThrowNotFoundException() {
        Artista artista = Artista.create(TEST_ARTISTA_1, TipoArtista.CANTOR);
        artista.persist();

        Album album = Album.create(TEST_ALBUM_1, 2020);
        album.artistas.add(artista);
        artista.albuns.add(album);
        album.persist();

        AlbumRequest request = new AlbumRequest();
        request.titulo = TEST_ALBUM_1;
        request.anoLancamento = 2020;
        request.artistaIds = List.of(999999L);

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> albumService.update(album.id, request)
        );

        assertTrue(exception.getMessage().contains("Artist with id"));
    }

    @Transactional
    Artista createAndPersistArtista(String nome, TipoArtista tipo) {
        Artista artista = Artista.create(nome, tipo);
        artista.persist();
        return artista;
    }
}
