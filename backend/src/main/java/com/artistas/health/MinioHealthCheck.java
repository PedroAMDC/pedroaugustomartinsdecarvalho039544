package com.artistas.health;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class MinioHealthCheck implements HealthCheck {

    private static final String HEALTH_CHECK_NAME = "MinIO connection";

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "minio.bucket")
    String bucket;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named(HEALTH_CHECK_NAME);

        try {
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build()
            );

            if (bucketExists) {
                responseBuilder.up()
                    .withData("bucket", bucket)
                    .withData("status", "available");
            } else {
                responseBuilder.down()
                    .withData("bucket", bucket)
                    .withData("error", "Bucket does not exist");
            }
        } catch (Exception e) {
            responseBuilder.down().withData("error", e.getMessage());
        }

        return responseBuilder.build();
    }
}
