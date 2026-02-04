package com.artistas.api.v1;

import com.artistas.schemas.CapaAlbumResponse;
import com.artistas.schemas.CapaPresignedUrlResponse;
import com.artistas.schemas.ErrorResponse;
import com.artistas.schemas.RateLimitErrorResponse;
import com.artistas.services.CapaService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/v1/albuns/{albumId}/capas")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Capas", description = "Album cover image management")
public class CapaResource {

    @Inject
    CapaService capaService;

    @POST
    @Authenticated
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Upload album cover", description = "Uploads a cover image for an album (authentication required)")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Cover uploaded successfully",
            content = @Content(schema = @Schema(implementation = CapaAlbumResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid file type or missing file",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Album not found",
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
    public Response upload(
        @Parameter(description = "Album ID", required = true)
        @PathParam("albumId") Long albumId,
        @RestForm("file") FileUpload file
    ) {
        CapaAlbumResponse response = capaService.upload(albumId, file);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{capaId}/url")
    @Operation(summary = "Get presigned URL", description = "Returns a presigned URL for downloading the cover image (30 min expiration)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Presigned URL generated",
            content = @Content(schema = @Schema(implementation = CapaPresignedUrlResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Cover or album not found",
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
    public Response getPresignedUrl(
        @Parameter(description = "Album ID", required = true)
        @PathParam("albumId") Long albumId,
        @Parameter(description = "Cover ID", required = true)
        @PathParam("capaId") Long capaId
    ) {
        CapaPresignedUrlResponse response = capaService.getPresignedUrl(albumId, capaId);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{capaId}")
    @Authenticated
    @Operation(summary = "Delete album cover", description = "Removes a cover image from MinIO and database (authentication required)")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Cover deleted successfully"),
        @APIResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Cover or album not found",
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
    public Response delete(
        @Parameter(description = "Album ID", required = true)
        @PathParam("albumId") Long albumId,
        @Parameter(description = "Cover ID", required = true)
        @PathParam("capaId") Long capaId
    ) {
        capaService.delete(albumId, capaId);
        return Response.noContent().build();
    }
}
