package com.artistas.schemas;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Paginated list of artists")
public class ArtistaListResponse {

    @Schema(description = "List of artists in current page")
    public List<ArtistaResponse> content;

    @Schema(description = "Current page number (0-indexed)", examples = {"0"})
    public Integer page;

    @Schema(description = "Page size", examples = {"10"})
    public Integer size;

    @Schema(description = "Total number of artists", examples = {"42"})
    public Long totalElements;

    @Schema(description = "Total number of pages", examples = {"5"})
    public Integer totalPages;

    public static ArtistaListResponse of(List<ArtistaResponse> content, Integer page, Integer size, Long totalElements) {
        ArtistaListResponse response = new ArtistaListResponse();
        response.content = content;
        response.page = page;
        response.size = size;
        response.totalElements = totalElements;
        response.totalPages = (int) Math.ceil((double) totalElements / size);
        return response;
    }
}
