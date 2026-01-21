package com.artistas.schemas;

public class CapaPresignedUrlResponse {

    public String url;
    public Integer expiresInSeconds;

    public static CapaPresignedUrlResponse of(String url, Integer expiresInSeconds) {
        CapaPresignedUrlResponse response = new CapaPresignedUrlResponse();
        response.url = url;
        response.expiresInSeconds = expiresInSeconds;
        return response;
    }
}
