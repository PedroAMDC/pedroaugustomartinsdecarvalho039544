package com.artistas.services;

import com.artistas.models.Usuario;
import com.artistas.schemas.TokenPair;
import com.artistas.services.exceptions.InvalidTokenException;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    @ConfigProperty(name = "smallrye.jwt.new-token.issuer", defaultValue = "artistas-albuns-api")
    String issuer;

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "300")
    Long accessTokenLifespanSeconds;

    @ConfigProperty(name = "jwt.refresh.expiration.seconds", defaultValue = "86400")
    Long refreshTokenLifespanSeconds;

    @Inject
    JWTParser jwtParser;

    public TokenPair generateTokenPair(Usuario usuario) {
        String accessToken = generateAccessToken(usuario);
        String refreshToken = generateRefreshToken(usuario);
        return new TokenPair(accessToken, accessTokenLifespanSeconds, refreshToken);
    }

    public String generateAccessToken(Usuario usuario) {
        return Jwt.issuer(issuer)
            .upn(usuario.email)
            .subject(usuario.id.toString())
            .claim("email", usuario.email)
            .claim("userId", usuario.id)
            .claim("type", "access")
            .groups(Set.of("user"))
            .expiresIn(Duration.ofSeconds(accessTokenLifespanSeconds))
            .sign();
    }

    public String generateRefreshToken(Usuario usuario) {
        return Jwt.issuer(issuer)
            .upn(usuario.email)
            .subject(usuario.id.toString())
            .claim("type", "refresh")
            .expiresIn(Duration.ofSeconds(refreshTokenLifespanSeconds))
            .sign();
    }

    public JsonWebToken validateAccessToken(String token) {
        JsonWebToken jwt = parseToken(token);
        validateTokenType(jwt, "access");
        return jwt;
    }

    public JsonWebToken validateRefreshToken(String token) {
        JsonWebToken jwt = parseToken(token);
        validateTokenType(jwt, "refresh");
        return jwt;
    }

    public TokenPair refreshTokenPair(String refreshToken) {
        JsonWebToken jwt = validateRefreshToken(refreshToken);
        Long userId = jwt.getClaim("userId");

        if (userId == null) {
            String subject = jwt.getSubject();
            userId = Long.parseLong(subject);
        }

        Usuario usuario = Usuario.findById(userId);

        if (usuario == null || !usuario.ativo) {
            throw new InvalidTokenException("User not found or inactive");
        }

        return generateTokenPair(usuario);
    }

    public Long getAccessTokenLifespan() {
        return accessTokenLifespanSeconds;
    }

    private JsonWebToken parseToken(String token) {
        try {
            return jwtParser.parse(token);
        } catch (ParseException e) {
            throw new InvalidTokenException("Invalid or expired token");
        }
    }

    private void validateTokenType(JsonWebToken jwt, String expectedType) {
        String tokenType = jwt.getClaim("type");
        if (!expectedType.equals(tokenType)) {
            throw new InvalidTokenException("Invalid token type");
        }
    }
}
