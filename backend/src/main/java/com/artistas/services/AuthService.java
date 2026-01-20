package com.artistas.services;

import com.artistas.models.Usuario;
import com.artistas.schemas.LoginRequest;
import com.artistas.schemas.LoginResponse;
import com.artistas.schemas.RegisterRequest;
import com.artistas.schemas.TokenPair;
import com.artistas.services.exceptions.AuthenticationException;
import com.artistas.services.exceptions.ConflictException;
import com.artistas.services.exceptions.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    @Inject
    TokenService tokenService;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = Usuario.findByEmail(request.email);

        if (usuario == null || !usuario.ativo) {
            throw new AuthenticationException("Invalid credentials");
        }

        if (!usuario.verifyPassword(request.password)) {
            throw new AuthenticationException("Invalid credentials");
        }

        TokenPair tokenPair = tokenService.generateTokenPair(usuario);

        return LoginResponse.of(
            tokenPair.accessToken(),
            tokenPair.expiresIn(),
            tokenPair.refreshToken()
        );
    }

    public LoginResponse refresh(String refreshToken) {
        TokenPair tokenPair = tokenService.refreshTokenPair(refreshToken);

        return LoginResponse.of(
            tokenPair.accessToken(),
            tokenPair.expiresIn(),
            tokenPair.refreshToken()
        );
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (!request.password.equals(request.confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }

        if (Usuario.findByEmail(request.email) != null) {
            throw new ConflictException("Email already registered");
        }

        Usuario usuario = Usuario.create(request.email, request.password, request.nome);
        usuario.persist();
    }
}
