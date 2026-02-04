package com.artistas.api.v1;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class HealthCheckResourceTest {

    @Test
    void health_shouldReturn200WithUpStatus() {
        given()
        .when()
            .get("/q/health")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    void healthReady_shouldReturn200WithUpStatus() {
        given()
        .when()
            .get("/q/health/ready")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("checks", notNullValue());
    }

    @Test
    void healthLive_shouldReturn200WithUpStatus() {
        given()
        .when()
            .get("/q/health/live")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }
}
