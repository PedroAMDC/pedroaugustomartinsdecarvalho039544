package com.artistas.testresources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MinIOContainer;
import java.util.HashMap;
import java.util.Map;

public class MinioTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String MINIO_IMAGE = "minio/minio:latest";
    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin123";
    private static final String BUCKET_NAME = "albuns-capas";

    private MinIOContainer minioContainer;

    @Override
    public Map<String, String> start() {
        minioContainer = new MinIOContainer(MINIO_IMAGE)
            .withUserName(ACCESS_KEY)
            .withPassword(SECRET_KEY);

        minioContainer.start();

        createBucket();

        Map<String, String> config = new HashMap<>();
        config.put("minio.endpoint", minioContainer.getS3URL());
        config.put("minio.public-endpoint", minioContainer.getS3URL());
        config.put("minio.access-key", ACCESS_KEY);
        config.put("minio.secret-key", SECRET_KEY);
        config.put("minio.bucket", BUCKET_NAME);

        return config;
    }

    private void createBucket() {
        io.minio.MinioClient client = null;
        try {
            client = io.minio.MinioClient.builder()
                .endpoint(minioContainer.getS3URL())
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();

            boolean bucketExists = client.bucketExists(
                io.minio.BucketExistsArgs.builder()
                    .bucket(BUCKET_NAME)
                    .build()
            );

            if (!bucketExists) {
                client.makeBucket(
                    io.minio.MakeBucketArgs.builder()
                        .bucket(BUCKET_NAME)
                        .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MinIO bucket", e);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void stop() {
        if (minioContainer != null) {
            minioContainer.stop();
        }
    }
}
