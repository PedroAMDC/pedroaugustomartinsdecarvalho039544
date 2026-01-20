package com.artistas.schemas;

public class LoginResponse {

    public String token;
    public Long expiresIn;
    public String refreshToken;

    public static LoginResponse of(String token, Long expiresIn, String refreshToken) {
        LoginResponse response = new LoginResponse();
        response.token = token;
        response.expiresIn = expiresIn;
        response.refreshToken = refreshToken;
        return response;
    }
}
