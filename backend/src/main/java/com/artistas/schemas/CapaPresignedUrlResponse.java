package com.artistas.schemas;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Presigned URL for cover image download")
public class CapaPresignedUrlResponse {

    @Schema(description = "Presigned URL for downloading the cover", examples = {"https://minio:9000/albuns-capas/capas/1/image.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256"})
    public String url;

    @Schema(description = "URL expiration time in seconds", examples = {"1800"})
    public Integer expiresInSeconds;

    public static CapaPresignedUrlResponse of(String url, Integer expiresInSeconds) {
        CapaPresignedUrlResponse response = new CapaPresignedUrlResponse();
        response.url = url;
        response.expiresInSeconds = expiresInSeconds;
        return response;
    }
}
