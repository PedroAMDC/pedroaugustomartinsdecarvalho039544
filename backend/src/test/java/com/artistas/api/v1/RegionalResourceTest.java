package com.artistas.api.v1;

import com.artistas.models.Regional;
import com.artistas.models.Usuario;
import com.artistas.schemas.RegionalApiResponse;
import com.artistas.services.RegionalApiClient;
import com.artistas.services.TokenService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@QuarkusTest
public class RegionalResourceTest {

    @Inject
    TokenService tokenService;

    @InjectMock
    @RestClient
    RegionalApiClient apiClient;

    private static final String TEST_EMAIL = "regionalresource@test.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "Test User";

    private String accessToken;

    @BeforeEach
    void setUp() {
        QuarkusTransaction.requiringNew().run(() -> {
            Regional.deleteAll();
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
    }

    @AfterEach
    void tearDown() {
        QuarkusTransaction.requiringNew().run(() -> {
            Regional.deleteAll();
            Usuario.delete("email", TEST_EMAIL);
        });
    }

    @Test
    void list_withNoFilter_shouldReturnAllRegionais() {
        QuarkusTransaction.requiringNew().run(() -> {
            Regional.create(1, "REGIONAL ATIVA").persist();
            Regional inactive = Regional.create(2, "REGIONAL INATIVA");
            inactive.ativo = false;
            inactive.persist();
        });

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/v1/regionais")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("content.size()", equalTo(2))
            .body("totalElements", equalTo(2));
    }

    @Test
    void list_withAtivoTrue_shouldReturnOnlyActiveRegionais() {
        QuarkusTransaction.requiringNew().run(() -> {
            Regional.create(1, "REGIONAL ATIVA").persist();
            Regional inactive = Regional.create(2, "REGIONAL INATIVA");
            inactive.ativo = false;
            inactive.persist();
        });

        given()
            .contentType(ContentType.JSON)
            .queryParam("ativo", true)
        .when()
            .get("/v1/regionais")
        .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("content[0].nome", equalTo("REGIONAL ATIVA"))
            .body("content[0].ativo", equalTo(true));
    }

    @Test
    void list_withAtivoFalse_shouldReturnOnlyInactiveRegionais() {
        QuarkusTransaction.requiringNew().run(() -> {
            Regional.create(1, "REGIONAL ATIVA").persist();
            Regional inactive = Regional.create(2, "REGIONAL INATIVA");
            inactive.ativo = false;
            inactive.persist();
        });

        given()
            .contentType(ContentType.JSON)
            .queryParam("ativo", false)
        .when()
            .get("/v1/regionais")
        .then()
            .statusCode(200)
            .body("content.size()", equalTo(1))
            .body("content[0].nome", equalTo("REGIONAL INATIVA"))
            .body("content[0].ativo", equalTo(false));
    }

    @Test
    void list_withEmptyDatabase_shouldReturnEmptyList() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/v1/regionais")
        .then()
            .statusCode(200)
            .body("content", empty())
            .body("totalElements", equalTo(0));
    }

    @Test
    void sync_withValidToken_shouldReturn200AndStats() {
        List<RegionalApiResponse> externalList = Arrays.asList(
            createApiResponse(1, "REGIONAL DE CUIABA"),
            createApiResponse(2, "REGIONAL DE RONDONOPOLIS")
        );
        when(apiClient.getRegionais()).thenReturn(externalList);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .post("/v1/regionais/sync")
        .then()
            .statusCode(200)
            .body("inserted", equalTo(2))
            .body("inactivated", equalTo(0))
            .body("updated", equalTo(0))
            .body("total", equalTo(2));
    }

    @Test
    void sync_withoutToken_shouldReturn401() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/v1/regionais/sync")
        .then()
            .statusCode(401);
    }

    @Test
    void sync_withExistingData_shouldReturnSyncStats() {
        QuarkusTransaction.requiringNew().run(() -> {
            Regional.create(1, "REGIONAL DE CUIABA").persist();
            Regional.create(3, "REGIONAL A SER INATIVADA").persist();
        });

        List<RegionalApiResponse> externalList = Arrays.asList(
            createApiResponse(1, "REGIONAL DE CUIABA"),
            createApiResponse(2, "REGIONAL DE RONDONOPOLIS")
        );
        when(apiClient.getRegionais()).thenReturn(externalList);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .post("/v1/regionais/sync")
        .then()
            .statusCode(200)
            .body("inserted", equalTo(1))
            .body("inactivated", equalTo(1))
            .body("updated", equalTo(0))
            .body("total", equalTo(2));
    }

    private RegionalApiResponse createApiResponse(Integer id, String nome) {
        RegionalApiResponse response = new RegionalApiResponse();
        response.id = id;
        response.nome = nome;
        return response;
    }
}
