package com.artistas.services;

import com.artistas.models.Usuario;
import com.artistas.schemas.TokenPair;
import com.artistas.services.exceptions.InvalidTokenException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TokenServiceTest {

    @Inject
    TokenService tokenService;

    private static final String TEST_EMAIL = "token-test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "Token Test User";

    private Usuario testUsuario;

    @BeforeEach
    @Transactional
    void setUp() {
        Usuario.delete("email", TEST_EMAIL);
        testUsuario = Usuario.create(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
        testUsuario.persist();
    }

    @AfterEach
    @Transactional
    void tearDown() {
        Usuario.delete("email", TEST_EMAIL);
    }

    @Test
    void generateTokenPair_shouldReturnValidTokens() {
        TokenPair tokenPair = tokenService.generateTokenPair(testUsuario);

        assertNotNull(tokenPair);
        assertNotNull(tokenPair.accessToken());
        assertNotNull(tokenPair.refreshToken());
        assertTrue(tokenPair.expiresIn() > 0);
    }

    @Test
    void generateAccessToken_shouldContainCorrectClaims() {
        String token = tokenService.generateAccessToken(testUsuario);
        JsonWebToken jwt = tokenService.validateAccessToken(token);

        assertEquals(testUsuario.id.toString(), jwt.getSubject());
        assertEquals(testUsuario.email, jwt.getClaim("email"));
        assertEquals("access", jwt.getClaim("type"));
        assertTrue(jwt.getGroups().contains("user"));
    }

    @Test
    void generateRefreshToken_shouldContainRefreshType() {
        String token = tokenService.generateRefreshToken(testUsuario);
        JsonWebToken jwt = tokenService.validateRefreshToken(token);

        assertEquals(testUsuario.id.toString(), jwt.getSubject());
        assertEquals("refresh", jwt.getClaim("type"));
    }

    @Test
    void validateAccessToken_withValidToken_shouldReturnJsonWebToken() {
        String token = tokenService.generateAccessToken(testUsuario);

        JsonWebToken jwt = tokenService.validateAccessToken(token);

        assertNotNull(jwt);
        assertEquals(testUsuario.id.toString(), jwt.getSubject());
    }

    @Test
    void validateAccessToken_withRefreshToken_shouldThrowInvalidTokenException() {
        String refreshToken = tokenService.generateRefreshToken(testUsuario);

        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> tokenService.validateAccessToken(refreshToken)
        );

        assertEquals("Invalid token type", exception.getMessage());
    }

    @Test
    void validateRefreshToken_withAccessToken_shouldThrowInvalidTokenException() {
        String accessToken = tokenService.generateAccessToken(testUsuario);

        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> tokenService.validateRefreshToken(accessToken)
        );

        assertEquals("Invalid token type", exception.getMessage());
    }

    @Test
    void validateAccessToken_withInvalidToken_shouldThrowInvalidTokenException() {
        String invalidToken = "invalid.token.here";

        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> tokenService.validateAccessToken(invalidToken)
        );

        assertEquals("Invalid or expired token", exception.getMessage());
    }

    @Test
    void refreshTokenPair_withValidRefreshToken_shouldReturnNewTokenPair() {
        String refreshToken = tokenService.generateRefreshToken(testUsuario);

        TokenPair newTokenPair = tokenService.refreshTokenPair(refreshToken);

        assertNotNull(newTokenPair);
        assertNotNull(newTokenPair.accessToken());
        assertNotNull(newTokenPair.refreshToken());
    }

    @Test
    void refreshTokenPair_withAccessToken_shouldThrowInvalidTokenException() {
        String accessToken = tokenService.generateAccessToken(testUsuario);

        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> tokenService.refreshTokenPair(accessToken)
        );

        assertEquals("Invalid token type", exception.getMessage());
    }

    @Test
    @Transactional
    void refreshTokenPair_withInactiveUser_shouldThrowInvalidTokenException() {
        String refreshToken = tokenService.generateRefreshToken(testUsuario);
        Usuario usuario = Usuario.findByEmail(TEST_EMAIL);
        usuario.ativo = false;

        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> tokenService.refreshTokenPair(refreshToken)
        );

        assertEquals("User not found or inactive", exception.getMessage());
    }

    @Test
    void getAccessTokenLifespan_shouldReturn300Seconds() {
        Long lifespan = tokenService.getAccessTokenLifespan();

        assertEquals(300L, lifespan);
    }
}
