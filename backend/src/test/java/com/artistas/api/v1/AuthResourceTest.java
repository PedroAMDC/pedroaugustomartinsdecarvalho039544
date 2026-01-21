package com.artistas.api.v1;

import com.artistas.models.Usuario;
import com.artistas.schemas.LoginRequest;
import com.artistas.schemas.RefreshRequest;
import com.artistas.schemas.RegisterRequest;
import com.artistas.services.TokenService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class AuthResourceTest {

    @Inject
    TokenService tokenService;

    private static final String TEST_EMAIL = "authresource@test.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "Test User";

    @AfterEach
    void tearDown() {
        QuarkusTransaction.requiringNew().run(() -> {
            Usuario.delete("email", TEST_EMAIL);
        });
    }

    private void createTestUser() {
        QuarkusTransaction.requiringNew().run(() -> {
            Usuario usuario = Usuario.create(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
            usuario.persist();
        });
    }

    private String generateRefreshToken() {
        return QuarkusTransaction.requiringNew().call(() -> {
            Usuario usuario = Usuario.findByEmail(TEST_EMAIL);
            return tokenService.generateTokenPair(usuario).refreshToken();
        });
    }

    @Test
    void login_withValidCredentials_shouldReturn200AndToken() {
        createTestUser();

        LoginRequest request = new LoginRequest();
        request.email = TEST_EMAIL;
        request.password = TEST_PASSWORD;

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("refreshToken", notNullValue())
            .body("expiresIn", greaterThan(0));
    }

    @Test
    void login_withInvalidCredentials_shouldReturn401() {
        LoginRequest request = new LoginRequest();
        request.email = "nonexistent@example.com";
        request.password = TEST_PASSWORD;

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(401)
            .body("error", is("Invalid credentials"));
    }

    @Test
    void login_withInvalidEmail_shouldReturn400() {
        LoginRequest request = new LoginRequest();
        request.email = "invalid-email";
        request.password = TEST_PASSWORD;

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void register_withValidData_shouldReturn201AndToken() {
        RegisterRequest request = new RegisterRequest();
        request.email = TEST_EMAIL;
        request.password = TEST_PASSWORD;
        request.confirmPassword = TEST_PASSWORD;
        request.nome = TEST_NAME;

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/v1/auth/register")
        .then()
            .statusCode(201)
            .body("token", notNullValue())
            .body("refreshToken", notNullValue())
            .body("expiresIn", greaterThan(0));
    }

    @Test
    void register_withExistingEmail_shouldReturn409() {
        createTestUser();

        RegisterRequest request = new RegisterRequest();
        request.email = TEST_EMAIL;
        request.password = "newpassword123";
        request.confirmPassword = "newpassword123";
        request.nome = "Another User";

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/v1/auth/register")
        .then()
            .statusCode(409)
            .body("error", is("Email already registered"));
    }

    @Test
    void register_withInvalidData_shouldReturn400() {
        RegisterRequest request = new RegisterRequest();
        request.email = "invalid-email";
        request.password = "short";
        request.confirmPassword = "short";
        request.nome = "";

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/v1/auth/register")
        .then()
            .statusCode(400);
    }

    @Test
    void refresh_withValidToken_shouldReturn200AndNewToken() {
        createTestUser();
        String refreshToken = generateRefreshToken();

        RefreshRequest request = new RefreshRequest();
        request.refreshToken = refreshToken;

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/v1/auth/refresh")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("refreshToken", notNullValue())
            .body("expiresIn", greaterThan(0));
    }

    @Test
    void refresh_withInvalidToken_shouldReturn401() {
        RefreshRequest request = new RefreshRequest();
        request.refreshToken = "invalid.token.here";

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put("/v1/auth/refresh")
        .then()
            .statusCode(401);
    }
}
