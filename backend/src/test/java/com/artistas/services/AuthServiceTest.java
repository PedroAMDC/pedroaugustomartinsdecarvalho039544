package com.artistas.services;

import com.artistas.models.Usuario;
import com.artistas.schemas.LoginRequest;
import com.artistas.schemas.LoginResponse;
import com.artistas.schemas.RegisterRequest;
import com.artistas.services.exceptions.AuthenticationException;
import com.artistas.services.exceptions.ConflictException;
import com.artistas.services.exceptions.ValidationException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class AuthServiceTest {

    @Inject
    AuthService authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "Test User";

    @BeforeEach
    @Transactional
    void setUp() {
        Usuario.delete("email", TEST_EMAIL);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        Usuario.delete("email", TEST_EMAIL);
    }

    @Test
    @Transactional
    void login_withValidCredentials_shouldReturnToken() {
        Usuario usuario = Usuario.create(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
        usuario.persist();

        LoginRequest request = new LoginRequest();
        request.email = TEST_EMAIL;
        request.password = TEST_PASSWORD;

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.token);
        assertNotNull(response.refreshToken);
        assertTrue(response.expiresIn > 0);
    }

    @Test
    void login_withInvalidEmail_shouldThrowAuthenticationException() {
        LoginRequest request = new LoginRequest();
        request.email = "nonexistent@example.com";
        request.password = TEST_PASSWORD;

        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> authService.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    @Transactional
    void login_withInvalidPassword_shouldThrowAuthenticationException() {
        Usuario usuario = Usuario.create(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
        usuario.persist();

        LoginRequest request = new LoginRequest();
        request.email = TEST_EMAIL;
        request.password = "wrongpassword";

        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> authService.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    @Transactional
    void login_withInactiveUser_shouldThrowAuthenticationException() {
        Usuario usuario = Usuario.create(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
        usuario.ativo = false;
        usuario.persist();

        LoginRequest request = new LoginRequest();
        request.email = TEST_EMAIL;
        request.password = TEST_PASSWORD;

        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> authService.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void register_withValidData_shouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.email = TEST_EMAIL;
        request.password = TEST_PASSWORD;
        request.confirmPassword = TEST_PASSWORD;
        request.nome = TEST_NAME;

        LoginResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.token);
        assertNotNull(response.refreshToken);
        assertTrue(response.expiresIn > 0);

        Usuario usuario = Usuario.findByEmail(TEST_EMAIL);
        assertNotNull(usuario);
        assertEquals(TEST_EMAIL, usuario.email);
        assertEquals(TEST_NAME, usuario.nome);
        assertTrue(usuario.ativo);
    }

    @Test
    @Transactional
    void register_withExistingEmail_shouldThrowConflictException() {
        Usuario existingUser = Usuario.create(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
        existingUser.persist();

        RegisterRequest request = new RegisterRequest();
        request.email = TEST_EMAIL;
        request.password = "newpassword123";
        request.confirmPassword = "newpassword123";
        request.nome = "Another User";

        ConflictException exception = assertThrows(
            ConflictException.class,
            () -> authService.register(request)
        );

        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void register_withMismatchedPasswords_shouldThrowValidationException() {
        RegisterRequest request = new RegisterRequest();
        request.email = TEST_EMAIL;
        request.password = TEST_PASSWORD;
        request.confirmPassword = "differentpassword";
        request.nome = TEST_NAME;

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> authService.register(request)
        );

        assertEquals("Passwords do not match", exception.getMessage());
    }
}
