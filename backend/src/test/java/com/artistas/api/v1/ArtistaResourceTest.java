package com.artistas.api.v1;

import com.artistas.models.Artista;
import com.artistas.models.TipoArtista;
import com.artistas.models.Usuario;
import com.artistas.schemas.ArtistaRequest;
import com.artistas.services.RateLimitService;
import com.artistas.services.TokenService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class ArtistaResourceTest {

    @Inject
    TokenService tokenService;

    @Inject
    RateLimitService rateLimitService;

    private static final String TEST_EMAIL = "artistaresource@test.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "Test User";

    private String accessToken;
    private Long artistaId;

    @BeforeEach
    void setUp() {
        rateLimitService.clearAll();

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
            Artista artista = Artista.create("Test Artist", TipoArtista.CANTOR);
            artista.persist();
            return artista.id;
        });
    }

    @AfterEach
    void tearDown() {
        QuarkusTransaction.requiringNew().run(() -> {
            Artista.delete("nome like ?1", "Test%");
            Usuario.delete("email", TEST_EMAIL);
        });
    }

    @Test
    void list_shouldReturn200AndPaginatedResult() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/v1/artistas")
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
            .get("/v1/artistas")
        .then()
            .statusCode(200)
            .body("page", equalTo(0))
            .body("size", equalTo(5));
    }

    @Test
    void list_withNameFilter_shouldReturnFilteredResults() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("nome", "Test")
        .when()
            .get("/v1/artistas")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalElements", greaterThanOrEqualTo(1));
    }

    @Test
    void list_withTypeFilter_shouldReturnFilteredResults() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("tipo", "CANTOR")
        .when()
            .get("/v1/artistas")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void list_withSorting_shouldReturnSortedResults() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("direction", "desc")
        .when()
            .get("/v1/artistas")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void findById_withValidId_shouldReturn200AndArtistWithAlbums() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/v1/artistas/{id}", artistaId)
        .then()
            .statusCode(200)
            .body("id", equalTo(artistaId.intValue()))
            .body("nome", equalTo("Test Artist"))
            .body("tipo", equalTo("CANTOR"))
            .body("albuns", notNullValue());
    }

    @Test
    void findById_withInvalidId_shouldReturn404() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/v1/artistas/{id}", 999999L)
        .then()
            .statusCode(404);
    }

    @Test
    void create_withValidToken_shouldReturn201() {
        ArtistaRequest request = new ArtistaRequest();
        request.nome = "Test New Artist";
        request.tipo = TipoArtista.BANDA;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .post("/v1/artistas")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo("Test New Artist"))
            .body("tipo", equalTo("BANDA"));
    }

    @Test
    void create_withoutToken_shouldReturn401() {
        ArtistaRequest request = new ArtistaRequest();
        request.nome = "Test Unauthorized Artist";
        request.tipo = TipoArtista.CANTOR;

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/v1/artistas")
        .then()
            .statusCode(401);
    }

    @Test
    void create_withInvalidData_shouldReturn400() {
        ArtistaRequest request = new ArtistaRequest();
        request.nome = "";
        request.tipo = null;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .post("/v1/artistas")
        .then()
            .statusCode(400);
    }

    @Test
    void update_withValidToken_shouldReturn200() {
        ArtistaRequest request = new ArtistaRequest();
        request.nome = "Test Updated Artist";
        request.tipo = TipoArtista.BANDA;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .put("/v1/artistas/{id}", artistaId)
        .then()
            .statusCode(200)
            .body("id", equalTo(artistaId.intValue()))
            .body("nome", equalTo("Test Updated Artist"))
            .body("tipo", equalTo("BANDA"));
    }

    @Test
    void update_withoutToken_shouldReturn401() {
        ArtistaRequest request = new ArtistaRequest();
        request.nome = "Test Unauthorized Update";
        request.tipo = TipoArtista.CANTOR;

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/v1/artistas/{id}", artistaId)
        .then()
            .statusCode(401);
    }

    @Test
    void update_withInvalidId_shouldReturn404() {
        ArtistaRequest request = new ArtistaRequest();
        request.nome = "Test Update Not Found";
        request.tipo = TipoArtista.CANTOR;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(request)
        .when()
            .put("/v1/artistas/{id}", 999999L)
        .then()
            .statusCode(404);
    }
}
