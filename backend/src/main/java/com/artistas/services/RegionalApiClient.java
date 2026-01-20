package com.artistas.services;

import com.artistas.schemas.RegionalApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;

@Path("/v1/regionais")
@RegisterRestClient(configKey = "regional-api")
@Produces(MediaType.APPLICATION_JSON)
public interface RegionalApiClient {

    @GET
    List<RegionalApiResponse> getRegionais();
}
