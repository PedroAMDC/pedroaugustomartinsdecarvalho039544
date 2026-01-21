package com.artistas.api.v1;

import com.artistas.models.TipoArtista;
import com.artistas.schemas.AlbumPaginatedResponse;
import com.artistas.schemas.AlbumRequest;
import com.artistas.schemas.AlbumResponse;
import com.artistas.services.AlbumService;
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

@Path("/v1/albuns")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Albuns", description = "CRUD operations for albums")
public class AlbumResource {

    @Inject
    AlbumService albumService;

    @GET
    @Operation(summary = "List albums", description = "Returns a paginated list of albums with optional filters")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of albums",
            content = @Content(schema = @Schema(implementation = AlbumPaginatedResponse.class))
        )
    })
    public Response list(
        @Parameter(description = "Page number (0-indexed)")
        @QueryParam("page") @DefaultValue("0") Integer page,
        @Parameter(description = "Page size")
        @QueryParam("size") @DefaultValue("10") Integer size,
        @Parameter(description = "Filter by artist ID")
        @QueryParam("artistaId") Long artistaId,
        @Parameter(description = "Filter by artist type (CANTOR or BANDA)")
        @QueryParam("tipoArtista") TipoArtista tipoArtista,
        @Parameter(description = "Sort direction (asc or desc)")
        @QueryParam("direction") @DefaultValue("asc") String direction
    ) {
        AlbumPaginatedResponse response = albumService.list(page, size, artistaId, tipoArtista, direction);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get album by ID", description = "Returns an album with associated artists and covers")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Album found",
            content = @Content(schema = @Schema(implementation = AlbumResponse.class))
        ),
        @APIResponse(responseCode = "404", description = "Album not found")
    })
    public Response findById(
        @Parameter(description = "Album ID", required = true)
        @PathParam("id") Long id
    ) {
        AlbumResponse response = albumService.findById(id);
        return Response.ok(response).build();
    }

    @POST
    @Authenticated
    @Operation(summary = "Create album", description = "Creates a new album with artists (authentication required)")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Album created",
            content = @Content(schema = @Schema(implementation = AlbumResponse.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid request data"),
        @APIResponse(responseCode = "401", description = "Authentication required"),
        @APIResponse(responseCode = "404", description = "Artist not found")
    })
    public Response create(@Valid AlbumRequest request) {
        AlbumResponse response = albumService.create(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @Authenticated
    @Operation(summary = "Update album", description = "Updates an existing album (authentication required)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Album updated",
            content = @Content(schema = @Schema(implementation = AlbumResponse.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid request data"),
        @APIResponse(responseCode = "401", description = "Authentication required"),
        @APIResponse(responseCode = "404", description = "Album or artist not found")
    })
    public Response update(
        @Parameter(description = "Album ID", required = true)
        @PathParam("id") Long id,
        @Valid AlbumRequest request
    ) {
        AlbumResponse response = albumService.update(id, request);
        return Response.ok(response).build();
    }
}
