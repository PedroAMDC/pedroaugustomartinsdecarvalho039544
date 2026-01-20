package com.artistas.services;

import com.artistas.services.exceptions.StorageException;
import com.artistas.services.exceptions.ValidationException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    );

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "minio.bucket")
    String bucket;

    @ConfigProperty(name = "minio.presigned-url-expiry-seconds", defaultValue = "1800")
    int presignedUrlExpirySeconds;

    public String uploadFile(InputStream inputStream, String filename, String contentType, long size) {
        validateImageType(contentType);

        String objectKey = generateObjectKey(filename);

        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to upload file: " + e.getMessage(), e);
        }

        return objectKey;
    }

    public String generatePresignedUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(presignedUrlExpirySeconds, TimeUnit.SECONDS)
                    .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    public void validateImageType(String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ValidationException("Invalid file type. Allowed types: " + String.join(", ", ALLOWED_CONTENT_TYPES));
        }
    }

    private String generateObjectKey(String filename) {
        String sanitizedFilename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return UUID.randomUUID() + "_" + sanitizedFilename;
    }
}
