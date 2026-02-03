package com.artistas.schemas;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Authentication response with JWT tokens")
public class LoginResponse {

    @Schema(description = "JWT access token", examples = {"eyJhbGciOiJSUzI1NiJ9..."})
    public String token;

    @Schema(description = "Token expiration time in seconds", examples = {"300"})
    public Long expiresIn;

    @Schema(description = "Refresh token for obtaining new access tokens", examples = {"eyJhbGciOiJSUzI1NiJ9..."})
    public String refreshToken;

    public static LoginResponse of(String token, Long expiresIn, String refreshToken) {
        LoginResponse response = new LoginResponse();
        response.token = token;
        response.expiresIn = expiresIn;
        response.refreshToken = refreshToken;
        return response;
    }
}
