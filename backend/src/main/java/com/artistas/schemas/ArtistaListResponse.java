package com.artistas.schemas;

import java.util.List;

public class ArtistaListResponse {

    public List<ArtistaResponse> content;
    public Integer page;
    public Integer size;
    public Long totalElements;
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
