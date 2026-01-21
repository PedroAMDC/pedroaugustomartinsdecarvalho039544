package com.artistas.schemas;

import java.util.List;

public class RegionalListResponse {

    public List<RegionalResponse> content;
    public Long totalElements;

    public static RegionalListResponse of(List<RegionalResponse> content) {
        RegionalListResponse response = new RegionalListResponse();
        response.content = content;
        response.totalElements = (long) content.size();
        return response;
    }
}
