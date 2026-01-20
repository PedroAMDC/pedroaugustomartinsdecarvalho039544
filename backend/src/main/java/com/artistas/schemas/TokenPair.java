package com.artistas.schemas;

public record TokenPair(
    String accessToken,
    Long expiresIn,
    String refreshToken
) {}
