package com.pizzaflow.tests.infrastructure;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

/**
 * Base class for PizzaFlow HTTP integration tests.
 *
 * <p>Subclasses configure {@code RestAssured.baseURI} and {@code RestAssured.port}
 * in a {@code @BeforeAll} method to point at the service under test.
 *
 * <p>When running in CI with services started via Docker Compose, set the
 * system properties {@code service.host} and {@code service.port}.
 *
 * <p>When running locally with services on localhost, the defaults apply.
 */
public abstract class BaseApiIT {

    protected static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(500);
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    @BeforeEach
    void configureRestAssured() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /** Convenience: GET endpoint and assert 200 */
    protected ValidatableResponse getOk(String path, Object... params) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get(path, params)
                .then()
                .statusCode(200);
    }

    /** Convenience: POST body and assert 201 */
    protected ValidatableResponse postCreated(String path, Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path)
                .then()
                .statusCode(201);
    }

    /** Convenience: POST body and assert 200 */
    protected ValidatableResponse postOk(String path, Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path)
                .then()
                .statusCode(200);
    }

    /**
     * Wait until the given assertion passes.
     * Useful for Kafka-async state changes.
     */
    protected void awaitUntil(Runnable assertion) {
        Awaitility.await()
                .pollInterval(DEFAULT_POLL_INTERVAL)
                .atMost(DEFAULT_TIMEOUT)
                .untilAsserted(assertion::run);
    }

    /** Build a base request spec for authenticated calls */
    protected RequestSpecification authenticatedRequest(String bearerToken) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + bearerToken);
    }
}
