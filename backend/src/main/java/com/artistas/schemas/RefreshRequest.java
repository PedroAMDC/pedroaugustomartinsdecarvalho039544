package com.artistas.schemas;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Token refresh request")
public class RefreshRequest {

    @NotBlank
    @Schema(description = "Refresh token obtained during login", examples = {"eyJhbGciOiJSUzI1NiJ9..."})
    public String refreshToken;
}
