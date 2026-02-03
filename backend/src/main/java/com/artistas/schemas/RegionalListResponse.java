package com.artistas.schemas;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "List of regionais")
public class RegionalListResponse {

    @Schema(description = "List of regionais")
    public List<RegionalResponse> content;

    @Schema(description = "Total number of regionais", examples = {"5"})
    public Long totalElements;

    public static RegionalListResponse of(List<RegionalResponse> content) {
        RegionalListResponse response = new RegionalListResponse();
        response.content = content;
        response.totalElements = (long) content.size();
        return response;
    }
}
