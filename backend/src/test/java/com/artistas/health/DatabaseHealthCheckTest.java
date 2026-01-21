package com.artistas.health;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class DatabaseHealthCheckTest {

    @Mock
    EntityManager entityManager;

    @Mock
    Query query;

    @InjectMocks
    DatabaseHealthCheck healthCheck;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void call_whenDatabaseIsUp_shouldReturnUp() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1);

        HealthCheckResponse response = healthCheck.call();

        assertEquals("Database connection", response.getName());
        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        assertTrue(response.getData().isPresent());
        assertEquals("PostgreSQL", response.getData().get().get("database"));
    }

    @Test
    void call_whenDatabaseIsDown_shouldReturnDown() {
        when(entityManager.createNativeQuery(anyString()))
            .thenThrow(new RuntimeException("Connection refused"));

        HealthCheckResponse response = healthCheck.call();

        assertEquals("Database connection", response.getName());
        assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
        assertTrue(response.getData().isPresent());
        assertTrue(response.getData().get().get("error").toString().contains("Connection refused"));
    }
}
