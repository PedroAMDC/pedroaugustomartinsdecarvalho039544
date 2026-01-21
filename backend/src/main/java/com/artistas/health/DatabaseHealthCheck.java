package com.artistas.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    private static final String HEALTH_CHECK_NAME = "Database connection";

    @Inject
    EntityManager entityManager;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named(HEALTH_CHECK_NAME);

        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            responseBuilder.up().withData("database", "PostgreSQL");
        } catch (Exception e) {
            responseBuilder.down().withData("error", e.getMessage());
        }

        return responseBuilder.build();
    }
}
