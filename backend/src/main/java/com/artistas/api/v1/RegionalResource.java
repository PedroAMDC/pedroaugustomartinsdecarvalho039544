package com.artistas.api.v1;

import com.artistas.schemas.ErrorResponse;
import com.artistas.schemas.RateLimitErrorResponse;
import com.artistas.schemas.RegionalListResponse;
import com.artistas.schemas.RegionalSyncResponse;
import com.artistas.services.RegionalService;
import com.artistas.services.RegionalSyncService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/regionais")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Regionais", description = "Operations for regional reference data")
public class RegionalResource {

    @Inject
    RegionalService regionalService;

    @Inject
    RegionalSyncService regionalSyncService;

    @GET
    @Operation(
        summary = "List regionais",
        description = "Returns a list of all regionais with optional status filter"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of regionais",
            content = @Content(schema = @Schema(implementation = RegionalListResponse.class))
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
        @Parameter(
            description = "Filter by active status. If not provided, returns all regionais",
            schema = @Schema(type = SchemaType.BOOLEAN)
        )
        @QueryParam("ativo") Boolean ativo
    ) {
        RegionalListResponse response = regionalService.list(ativo);
        return Response.ok(response).build();
    }

    @POST
    @Path("/sync")
    @Authenticated
    @Operation(
        summary = "Synchronize regionais",
        description = "Triggers synchronization with external API (authentication required)"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Synchronization completed",
            content = @Content(schema = @Schema(implementation = RegionalSyncResponse.class))
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
        ),
        @APIResponse(
            responseCode = "503",
            description = "External API unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response sync() {
        RegionalSyncResponse response = regionalSyncService.sync();
        return Response.ok(response).build();
    }
}
