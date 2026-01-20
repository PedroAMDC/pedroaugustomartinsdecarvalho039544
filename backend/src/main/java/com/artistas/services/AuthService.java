package com.artistas.services;

import com.artistas.models.Usuario;
import com.artistas.schemas.LoginRequest;
import com.artistas.schemas.LoginResponse;
import com.artistas.schemas.RegisterRequest;
import com.artistas.services.exceptions.AuthenticationException;
import com.artistas.services.exceptions.ConflictException;
import com.artistas.services.exceptions.ValidationException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    @ConfigProperty(name = "smallrye.jwt.new-token.issuer", defaultValue = "artistas-albuns-api")
    String issuer;

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "300")
    Long tokenLifespanSeconds;

    @ConfigProperty(name = "jwt.refresh.expiration.seconds", defaultValue = "86400")
    Long refreshTokenLifespanSeconds;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = Usuario.findByEmail(request.email);

        if (usuario == null || !usuario.ativo) {
            throw new AuthenticationException("Invalid credentials");
        }

        if (!usuario.verifyPassword(request.password)) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = generateToken(usuario);
        String refreshToken = generateRefreshToken(usuario);

        return LoginResponse.of(token, tokenLifespanSeconds, refreshToken);
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

    private String generateToken(Usuario usuario) {
        return Jwt.issuer(issuer)
            .upn(usuario.email)
            .subject(usuario.id.toString())
            .claim("email", usuario.email)
            .claim("userId", usuario.id)
            .groups(Set.of("user"))
            .expiresIn(Duration.ofSeconds(tokenLifespanSeconds))
            .sign();
    }

    private String generateRefreshToken(Usuario usuario) {
        return Jwt.issuer(issuer)
            .upn(usuario.email)
            .subject(usuario.id.toString())
            .claim("type", "refresh")
            .expiresIn(Duration.ofSeconds(refreshTokenLifespanSeconds))
            .sign();
    }
}
