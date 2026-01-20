package com.artistas.schemas;

import java.util.List;

public class AlbumPaginatedResponse {

    public List<AlbumResponse> content;
    public Integer page;
    public Integer size;
    public Long totalElements;
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
