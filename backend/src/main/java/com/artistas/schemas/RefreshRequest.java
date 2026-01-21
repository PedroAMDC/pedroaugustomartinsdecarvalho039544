package com.artistas.schemas;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequest {

    @NotBlank
    public String refreshToken;
}
