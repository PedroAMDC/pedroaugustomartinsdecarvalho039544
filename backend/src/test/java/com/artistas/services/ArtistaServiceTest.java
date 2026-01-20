package com.artistas.services;

import com.artistas.models.Artista;
import com.artistas.models.TipoArtista;
import com.artistas.schemas.ArtistaListResponse;
import com.artistas.schemas.ArtistaRequest;
import com.artistas.schemas.ArtistaResponse;
import com.artistas.services.exceptions.NotFoundException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ArtistaServiceTest {

    @Inject
    ArtistaService artistaService;

    private static final String TEST_NOME_1 = "Test Artist Alpha";
    private static final String TEST_NOME_2 = "Test Artist Beta";
    private static final String TEST_NOME_3 = "Test Artist Gamma";

    @BeforeEach
    @Transactional
    void setUp() {
        Artista.delete("nome like ?1", "Test Artist%");
    }

    @AfterEach
    @Transactional
    void tearDown() {
        Artista.delete("nome like ?1", "Test Artist%");
    }

    @Test
    @Transactional
    void list_withPagination_shouldReturnPaginatedResults() {
        Artista.create(TEST_NOME_1, TipoArtista.CANTOR).persist();
        Artista.create(TEST_NOME_2, TipoArtista.BANDA).persist();
        Artista.create(TEST_NOME_3, TipoArtista.CANTOR).persist();

        ArtistaListResponse response = artistaService.list(0, 2, null, null, null);

        assertNotNull(response);
        assertEquals(2, response.content.size());
        assertEquals(0, response.page);
        assertEquals(2, response.size);
        assertTrue(response.totalElements >= 3);
    }

    @Test
    @Transactional
    void list_withNameFilter_shouldReturnFilteredResults() {
        Artista.create(TEST_NOME_1, TipoArtista.CANTOR).persist();
        Artista.create(TEST_NOME_2, TipoArtista.BANDA).persist();

        ArtistaListResponse response = artistaService.list(0, 10, "alpha", null, null);

        assertNotNull(response);
        assertEquals(1, response.content.size());
        assertTrue(response.content.get(0).nome.toLowerCase().contains("alpha"));
    }

    @Test
    @Transactional
    void list_withTypeFilter_shouldReturnFilteredResults() {
        Artista.create(TEST_NOME_1, TipoArtista.CANTOR).persist();
        Artista.create(TEST_NOME_2, TipoArtista.BANDA).persist();
        Artista.create(TEST_NOME_3, TipoArtista.CANTOR).persist();

        ArtistaListResponse response = artistaService.list(0, 10, "Test Artist", TipoArtista.CANTOR, null);

        assertNotNull(response);
        assertEquals(2, response.content.size());
        response.content.forEach(artista -> assertEquals(TipoArtista.CANTOR, artista.tipo));
    }

    @Test
    @Transactional
    void list_withSortAsc_shouldReturnSortedResults() {
        Artista.create(TEST_NOME_3, TipoArtista.CANTOR).persist();
        Artista.create(TEST_NOME_1, TipoArtista.CANTOR).persist();
        Artista.create(TEST_NOME_2, TipoArtista.CANTOR).persist();

        ArtistaListResponse response = artistaService.list(0, 10, "Test Artist", null, "asc");

        assertNotNull(response);
        assertEquals(3, response.content.size());
        assertEquals(TEST_NOME_1, response.content.get(0).nome);
        assertEquals(TEST_NOME_2, response.content.get(1).nome);
        assertEquals(TEST_NOME_3, response.content.get(2).nome);
    }

    @Test
    @Transactional
    void list_withSortDesc_shouldReturnSortedResults() {
        Artista.create(TEST_NOME_1, TipoArtista.CANTOR).persist();
        Artista.create(TEST_NOME_3, TipoArtista.CANTOR).persist();
        Artista.create(TEST_NOME_2, TipoArtista.CANTOR).persist();

        ArtistaListResponse response = artistaService.list(0, 10, "Test Artist", null, "desc");

        assertNotNull(response);
        assertEquals(3, response.content.size());
        assertEquals(TEST_NOME_3, response.content.get(0).nome);
        assertEquals(TEST_NOME_2, response.content.get(1).nome);
        assertEquals(TEST_NOME_1, response.content.get(2).nome);
    }

    @Test
    @Transactional
    void findById_withExistingId_shouldReturnArtista() {
        Artista artista = Artista.create(TEST_NOME_1, TipoArtista.CANTOR);
        artista.persist();

        ArtistaResponse response = artistaService.findById(artista.id);

        assertNotNull(response);
        assertEquals(artista.id, response.id);
        assertEquals(TEST_NOME_1, response.nome);
        assertEquals(TipoArtista.CANTOR, response.tipo);
    }

    @Test
    void findById_withNonExistingId_shouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> artistaService.findById(999999L)
        );

        assertEquals("Artist not found", exception.getMessage());
    }

    @Test
    void create_withValidRequest_shouldCreateArtista() {
        ArtistaRequest request = new ArtistaRequest();
        request.nome = TEST_NOME_1;
        request.tipo = TipoArtista.BANDA;

        ArtistaResponse response = artistaService.create(request);

        assertNotNull(response);
        assertNotNull(response.id);
        assertEquals(TEST_NOME_1, response.nome);
        assertEquals(TipoArtista.BANDA, response.tipo);
        assertNotNull(response.createdAt);
    }

    @Test
    @Transactional
    void update_withValidRequest_shouldUpdateArtista() {
        Artista artista = Artista.create(TEST_NOME_1, TipoArtista.CANTOR);
        artista.persist();

        ArtistaRequest request = new ArtistaRequest();
        request.nome = TEST_NOME_2;
        request.tipo = TipoArtista.BANDA;

        ArtistaResponse response = artistaService.update(artista.id, request);

        assertNotNull(response);
        assertEquals(artista.id, response.id);
        assertEquals(TEST_NOME_2, response.nome);
        assertEquals(TipoArtista.BANDA, response.tipo);
    }

    @Test
    void update_withNonExistingId_shouldThrowNotFoundException() {
        ArtistaRequest request = new ArtistaRequest();
        request.nome = TEST_NOME_1;
        request.tipo = TipoArtista.CANTOR;

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> artistaService.update(999999L, request)
        );

        assertEquals("Artist not found", exception.getMessage());
    }
}
