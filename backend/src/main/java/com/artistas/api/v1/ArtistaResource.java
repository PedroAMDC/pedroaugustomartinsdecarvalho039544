package com.artistas.api.v1;

import com.artistas.models.TipoArtista;
import com.artistas.schemas.ArtistaDetailResponse;
import com.artistas.schemas.ArtistaListResponse;
import com.artistas.schemas.ArtistaRequest;
import com.artistas.schemas.ArtistaResponse;
import com.artistas.schemas.ErrorResponse;
import com.artistas.schemas.RateLimitErrorResponse;
import com.artistas.services.ArtistaService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/artistas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Artistas", description = "CRUD operations for artists")
public class ArtistaResource {

    @Inject
    ArtistaService artistaService;

    @GET
    @Operation(summary = "List artists", description = "Returns a paginated list of artists with optional filters")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of artists",
            content = @Content(schema = @Schema(implementation = ArtistaListResponse.class))
        ),
        @APIResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = RateLimitErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response list(
        @Parameter(description = "Page number (0-indexed)")
        @QueryParam("page") @DefaultValue("0") Integer page,
        @Parameter(description = "Page size")
        @QueryParam("size") @DefaultValue("10") Integer size,
        @Parameter(description = "Filter by name (case-insensitive, partial match)")
        @QueryParam("nome") String nome,
        @Parameter(description = "Filter by type (CANTOR or BANDA)")
        @QueryParam("tipo") TipoArtista tipo,
        @Parameter(description = "Sort field")
        @QueryParam("sort") @DefaultValue("nome") String sort,
        @Parameter(description = "Sort direction (asc or desc)")
        @QueryParam("direction") @DefaultValue("asc") String direction
    ) {
        ArtistaListResponse response = artistaService.list(page, size, nome, tipo, direction);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get artist by ID", description = "Returns an artist with associated albums")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Artist found",
            content = @Content(schema = @Schema(implementation = ArtistaDetailResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Artist not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = RateLimitErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response findById(
        @Parameter(description = "Artist ID", required = true)
        @PathParam("id") Long id
    ) {
        ArtistaDetailResponse response = artistaService.findByIdWithAlbuns(id);
        return Response.ok(response).build();
    }

    @POST
    @Authenticated
    @Operation(summary = "Create artist", description = "Creates a new artist (authentication required)")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Artist created",
            content = @Content(schema = @Schema(implementation = ArtistaResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = RateLimitErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response create(@Valid ArtistaRequest request) {
        ArtistaResponse response = artistaService.create(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @Authenticated
    @Operation(summary = "Update artist", description = "Updates an existing artist (authentication required)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Artist updated",
            content = @Content(schema = @Schema(implementation = ArtistaResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Artist not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = RateLimitErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response update(
        @Parameter(description = "Artist ID", required = true)
        @PathParam("id") Long id,
        @Valid ArtistaRequest request
    ) {
        ArtistaResponse response = artistaService.update(id, request);
        return Response.ok(response).build();
    }
}
