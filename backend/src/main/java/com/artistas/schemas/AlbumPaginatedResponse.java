package com.artistas.schemas;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Paginated list of albums")
public class AlbumPaginatedResponse {

    @Schema(description = "List of albums in current page")
    public List<AlbumResponse> content;

    @Schema(description = "Current page number (0-indexed)", examples = {"0"})
    public Integer page;

    @Schema(description = "Page size", examples = {"10"})
    public Integer size;

    @Schema(description = "Total number of albums", examples = {"100"})
    public Long totalElements;

    @Schema(description = "Total number of pages", examples = {"10"})
    public Integer totalPages;

    public static AlbumPaginatedResponse of(List<AlbumResponse> content, Integer page, Integer size, Long totalElements) {
        AlbumPaginatedResponse response = new AlbumPaginatedResponse();
        response.content = content;
        response.page = page;
        response.size = size;
        response.totalElements = totalElements;
        response.totalPages = (int) Math.ceil((double) totalElements / size);
        return response;
    }
}
