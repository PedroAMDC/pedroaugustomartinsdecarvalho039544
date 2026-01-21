package com.artistas.api.v1;

import com.artistas.models.Album;
import com.artistas.models.Artista;
import com.artistas.models.TipoArtista;
import com.artistas.models.Usuario;
import com.artistas.schemas.AlbumRequest;
import com.artistas.services.TokenService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class AlbumResourceTest {

    @Inject
    TokenService tokenService;

    private static final String TEST_EMAIL = "albumresource@test.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "Test User";

    private String accessToken;
    private Long albumId;
    private Long artistaId;

    @BeforeEach
    void setUp() {
        QuarkusTransaction.requiringNew().run(() -> {
            Usuario usuario = Usuario.findByEmail(TEST_EMAIL);
            if (usuario == null) {
                usuario = Usuario.create(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
                usuario.persist();
            }
        });

        accessToken = QuarkusTransaction.requiringNew().call(() -> {
            Usuario usuario = Usuario.findByEmail(TEST_EMAIL);
            return tokenService.generateTokenPair(usuario).accessToken();
        });

        artistaId = QuarkusTransaction.requiringNew().call(() -> {
            Artista artista = Artista.create("Test Artist for Album", TipoArtista.CANTOR);
            artista.persist();
            return artista.id;
        });

        albumId = QuarkusTransaction.requiringNew().call(() -> {
            Artista artista = Artista.findById(artistaId);
            Album album = Album.create("Test Album", 2020);
            album.artistas.add(artista);
            artista.albuns.add(album);
            album.persist();
            return album.id;
        });
    }

    @AfterEach
    void tearDown() {
        QuarkusTransaction.requiringNew().run(() -> {
            Album.find("titulo like ?1", "Test%").stream().forEach(a -> {
                Album album = (Album) a;
                for (Artista artista : album.artistas) {
                    artista.albuns.remove(album);
                }
                album.artistas.clear();
                album.delete();
            });
            Artista.delete("nome like ?1", "Test%");
            Usuario.delete("email", TEST_EMAIL);
        });
    }

    @Test
    void list_shouldReturn200AndPaginatedResult() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/v1/albuns")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("page", equalTo(0))
            .body("size", equalTo(10))
            .body("totalElements", greaterThanOrEqualTo(1));
    }

    @Test
    void list_withPagination_shouldReturnRequestedPage() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("size", 5)
        .when()
            .get("/v1/albuns")
        .then()
            .statusCode(200)
            .body("page", equalTo(0))
            .body("size", equalTo(5));
    }

    @Test
    void list_withArtistaIdFilter_shouldReturnFilteredResults() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("artistaId", artistaId)
        .when()
            .get("/v1/albuns")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalElements", greaterThanOrEqualTo(1));
    }

    @Test
    void list_withTipoArtistaFilter_shouldReturnFilteredResults() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("tipoArtista", "CANTOR")
        .when()
            .get("/v1/albuns")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void list_withSortingDesc_shouldReturnSortedResults() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("direction", "desc")
        .when()
            .get("/v1/albuns")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void list_withCombinedFilters_shouldApplyAllFilters() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("artistaId", artistaId)
            .queryParam("tipoArtista", "CANTOR")
            .queryParam("direction", "asc")
        .when()
            .get("/v1/albuns")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void findById_withValidId_shouldReturn200AndAlbumWithArtistasAndCapas() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/v1/albuns/{id}", albumId)
        .then()
            .statusCode(200)
            .body("id", equalTo(albumId.intValue()))
            .body("titulo", equalTo("Test Album"))
            .body("anoLancamento", equalTo(2020))
            .body("artistas", notNullValue())
            .body("capas", notNullValue());
    }

    @Test
    void findById_withInvalidId_shouldReturn404() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/v1/albuns/{id}", 999999L)
        .then()
            .statusCode(404);
    }

    @Test
    void create_withValidToken_shouldReturn201() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = "Test New Album";
        request.anoLancamento = 2023;
        request.artistaIds = List.of(artistaId);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .post("/v1/albuns")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("titulo", equalTo("Test New Album"))
            .body("anoLancamento", equalTo(2023))
            .body("artistas", hasSize(1));
    }

    @Test
    void create_withoutToken_shouldReturn401() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = "Test Unauthorized Album";
        request.anoLancamento = 2023;
        request.artistaIds = List.of(artistaId);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/v1/albuns")
        .then()
            .statusCode(401);
    }

    @Test
    void create_withInvalidData_shouldReturn400() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = "";
        request.anoLancamento = null;
        request.artistaIds = List.of();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .post("/v1/albuns")
        .then()
            .statusCode(400);
    }

    @Test
    void create_withNonExistentArtista_shouldReturn404() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = "Test Album Orphan";
        request.anoLancamento = 2023;
        request.artistaIds = List.of(999999L);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .post("/v1/albuns")
        .then()
            .statusCode(404);
    }

    @Test
    void create_withInvalidYear_shouldReturn400() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = "Test Album Invalid Year";
        request.anoLancamento = 1800;
        request.artistaIds = List.of(artistaId);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .post("/v1/albuns")
        .then()
            .statusCode(400);
    }

    @Test
    void update_withValidToken_shouldReturn200() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = "Test Updated Album";
        request.anoLancamento = 2024;
        request.artistaIds = List.of(artistaId);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .put("/v1/albuns/{id}", albumId)
        .then()
            .statusCode(200)
            .body("id", equalTo(albumId.intValue()))
            .body("titulo", equalTo("Test Updated Album"))
            .body("anoLancamento", equalTo(2024));
    }

    @Test
    void update_withoutToken_shouldReturn401() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = "Test Unauthorized Update";
        request.anoLancamento = 2024;
        request.artistaIds = List.of(artistaId);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/v1/albuns/{id}", albumId)
        .then()
            .statusCode(401);
    }

    @Test
    void update_withInvalidId_shouldReturn404() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = "Test Update Not Found";
        request.anoLancamento = 2024;
        request.artistaIds = List.of(artistaId);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .put("/v1/albuns/{id}", 999999L)
        .then()
            .statusCode(404);
    }

    @Test
    void update_withNonExistentArtista_shouldReturn404() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = "Test Album Update Orphan";
        request.anoLancamento = 2024;
        request.artistaIds = List.of(999999L);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .put("/v1/albuns/{id}", albumId)
        .then()
            .statusCode(404);
    }

    @Test
    void update_withInvalidData_shouldReturn400() {
        AlbumRequest request = new AlbumRequest();
        request.titulo = "";
        request.anoLancamento = 2500;
        request.artistaIds = List.of();

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .put("/v1/albuns/{id}", albumId)
        .then()
            .statusCode(400);
    }
}
