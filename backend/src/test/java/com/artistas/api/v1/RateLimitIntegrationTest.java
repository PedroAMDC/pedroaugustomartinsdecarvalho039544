package com.artistas.api.v1;

import com.artistas.services.RateLimitService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class RateLimitIntegrationTest {

    @Inject
    RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService.clearAll();
    }

    @Test
    void apiEndpoint_shouldIncludeRateLimitHeaders() {
        given()
        .when()
            .get("/v1/artistas")
        .then()
            .statusCode(200)
            .header("X-RateLimit-Limit", notNullValue())
            .header("X-RateLimit-Remaining", notNullValue())
            .header("X-RateLimit-Reset", notNullValue());
    }
}
