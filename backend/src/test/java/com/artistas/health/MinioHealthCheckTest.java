package com.artistas.health;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MinioHealthCheckTest {

    @Mock
    MinioClient minioClient;

    @InjectMocks
    MinioHealthCheck healthCheck;

    private static final String TEST_BUCKET = "test-bucket";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        Field bucketField = MinioHealthCheck.class.getDeclaredField("bucket");
        bucketField.setAccessible(true);
        bucketField.set(healthCheck, TEST_BUCKET);
    }

    @Test
    void call_whenMinioIsUpAndBucketExists_shouldReturnUp() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        HealthCheckResponse response = healthCheck.call();

        assertEquals("MinIO connection", response.getName());
        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        assertTrue(response.getData().isPresent());
        assertEquals(TEST_BUCKET, response.getData().get().get("bucket"));
        assertEquals("available", response.getData().get().get("status"));
    }

    @Test
    void call_whenBucketDoesNotExist_shouldReturnDown() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        HealthCheckResponse response = healthCheck.call();

        assertEquals("MinIO connection", response.getName());
        assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertTrue(response.getData().isPresent());
        assertEquals(TEST_BUCKET, response.getData().get().get("bucket"));
        assertEquals("Bucket does not exist", response.getData().get().get("error"));
    }

    @Test
    void call_whenMinioThrowsException_shouldReturnDown() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
            .thenThrow(new RuntimeException("Connection refused"));

        HealthCheckResponse response = healthCheck.call();

        assertEquals("MinIO connection", response.getName());
        assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertTrue(response.getData().isPresent());
        assertTrue(response.getData().get().get("error").toString().contains("Connection refused"));
    }
}
