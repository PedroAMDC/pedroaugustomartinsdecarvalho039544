package com.artistas.schemas;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "New user registration data")
public class RegisterRequest {

    @NotBlank
    @Email
    @Schema(description = "User email address", examples = {"novo.usuario@email.com"})
    public String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Password (minimum 8 characters)", examples = {"Senha@123"})
    public String password;

    @NotBlank
    @Schema(description = "Password confirmation (must match password)", examples = {"Senha@123"})
    public String confirmPassword;

    @NotBlank
    @Schema(description = "User display name", examples = {"John Doe"})
    public String nome;
}
