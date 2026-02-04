package com.artistas.schemas;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "User login credentials")
public class LoginRequest {

    @NotBlank
    @Email
    @Schema(description = "User email address", examples = {"usuario@email.com"})
    public String email;

    @NotBlank
    @Schema(description = "User password", examples = {"Senha@123"})
    public String password;
}
