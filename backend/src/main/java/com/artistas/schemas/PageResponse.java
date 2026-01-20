package com.artistas.schemas;

import java.util.List;

public class PageResponse<T> {

    public List<T> content;
    public Integer page;
    public Integer size;
    public Long totalElements;
    public Integer totalPages;

    public static <T> PageResponse<T> of(List<T> content, Integer page, Integer size, Long totalElements) {
        PageResponse<T> response = new PageResponse<>();
        response.content = content;
        response.page = page;
        response.size = size;
        response.totalElements = totalElements;
        response.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return response;
    }
}
