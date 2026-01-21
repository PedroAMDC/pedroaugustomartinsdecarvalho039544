package com.artistas.api.v1;

import com.artistas.schemas.LoginRequest;
import com.artistas.schemas.LoginResponse;
import com.artistas.schemas.RefreshRequest;
import com.artistas.schemas.RegisterRequest;
import com.artistas.services.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @Operation(summary = "User login", description = "Authenticates user with email and password")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid request data"),
        @APIResponse(responseCode = "401", description = "Invalid credentials")
    })
    public Response login(@Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Response.ok(response).build();
    }

    @POST
    @Path("/register")
    @Operation(summary = "User registration", description = "Registers a new user and returns authentication token")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid request data"),
        @APIResponse(responseCode = "409", description = "Email already registered")
    })
    public Response register(@Valid RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/refresh")
    @Operation(summary = "Refresh token", description = "Renews access token using refresh token")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid request data"),
        @APIResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public Response refresh(@Valid RefreshRequest request) {
        LoginResponse response = authService.refresh(request.refreshToken);
        return Response.ok(response).build();
    }
}
