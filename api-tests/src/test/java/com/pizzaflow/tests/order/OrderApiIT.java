package com.pizzaflow.tests.order;

import com.pizzaflow.tests.infrastructure.BaseApiIT;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * End-to-end integration tests for the Order Service.
 *
 * <p><strong>Prerequisites:</strong></p>
 * <ul>
 *   <li>Order Service running on {@code ORDER_SERVICE_URL} (default: {@code http://localhost:8081})</li>
 *   <li>PostgreSQL accessible to the service</li>
 *   <li>Kafka accessible to the service</li>
 * </ul>
 *
 * <p><strong>To run:</strong></p>
 * <pre>
 *   # Start infrastructure and services first:
 *   docker-compose -f infrastructure/docker/docker-compose.yml up -d
 *
 *   # Then run:
 *   mvn verify -pl api-tests -Pintegration-tests
 * </pre>
 *
 * <p><strong>Custom base URL:</strong></p>
 * <pre>
 *   mvn verify -pl api-tests -Pintegration-tests -Dorder.service.url=http://myhost:8081
 * </pre>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderApiIT extends BaseApiIT {

    private static final String BASE_URL =
            System.getProperty("order.service.url", "http://localhost:8081");

    /** Shared state: order ID created in test 1, reused in subsequent tests */
    private static Long createdOrderId;
    private static String createdOrderNumber;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = "";
    }

    // =========================================================
    // Happy-path flow: Create → Retrieve → Update → Cancel
    // =========================================================

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("POST /api/v1/orders — create a new order")
    void createOrder_returnsCreatedWithOrderId() {
        Map<String, Object> createRequest = Map.of(
                "customerId", 1001L,
                "restaurantId", 1L,
                "deliveryAddress", Map.of(
                        "street", "123 Main St",
                        "city", "Pizza Town",
                        "postalCode", "12345"
                ),
                "items", List.of(
                        Map.of(
                                "menuItemId", 1L,
                                "name", "Margherita Pizza",
                                "quantity", 2,
                                "unitPrice", "12.99"
                        )
                )
        );

        ValidatableResponse response = postCreated("/api/v1/orders", createRequest);

        createdOrderId = response
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.orderNumber", notNullValue())
                .body("data.status", equalTo("PENDING"))
                .body("data.customerId", equalTo(1001))
                .extract().jsonPath().getLong("data.id");

        createdOrderNumber = given()
                .get("/api/v1/orders/" + createdOrderId)
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("data.orderNumber");

        Assertions.assertNotNull(createdOrderId, "Order ID must not be null after creation");
        Assertions.assertNotNull(createdOrderNumber, "Order number must not be null after creation");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("GET /api/v1/orders/{id} — retrieve order by ID")
    void getOrder_returnsOrderDetails() {
        Assumptions.assumeTrue(createdOrderId != null, "Requires order created in test 1");

        getOk("/api/v1/orders/{id}", createdOrderId)
                .body("data.id", equalTo(createdOrderId.intValue()))
                .body("data.status", equalTo("PENDING"))
                .body("data.items", hasSize(greaterThan(0)));
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("GET /api/v1/orders/number/{number} — retrieve order by order number")
    void getOrderByNumber_returnsOrderDetails() {
        Assumptions.assumeTrue(createdOrderNumber != null, "Requires order created in test 1");

        getOk("/api/v1/orders/number/{number}", createdOrderNumber)
                .body("data.orderNumber", equalTo(createdOrderNumber));
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("GET /api/v1/orders/customer/{customerId} — list customer orders")
    void getCustomerOrders_returnsList() {
        Assumptions.assumeTrue(createdOrderId != null, "Requires order created in test 1");

        getOk("/api/v1/orders/customer/{customerId}", 1001L)
                .body("data", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("POST /api/v1/orders/{id}/cancel — cancel the order")
    void cancelOrder_returnsSuccess() {
        Assumptions.assumeTrue(createdOrderId != null, "Requires order created in test 1");

        given()
                .queryParam("reason", "Integration test cancellation")
                .when()
                .post("/api/v1/orders/{id}/cancel", createdOrderId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    // =========================================================
    // V2 CQRS API tests
    // =========================================================

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("POST /api/v2/orders/commands/place — place order via CQRS command")
    void cqrsPlaceOrder_returnsAccepted() {
        Map<String, Object> placeCommand = Map.of(
                "customerId", "550e8400-e29b-41d4-a716-446655440001",
                "restaurantId", "550e8400-e29b-41d4-a716-446655440002",
                "orderType", "DELIVERY",
                "deliveryAddressStreet", "456 Test Ave",
                "deliveryAddressCity", "Test City",
                "deliveryAddressPostalCode", "99999",
                "items", List.of(
                        Map.of(
                                "itemId", "item-001",
                                "itemName", "Pepperoni Pizza",
                                "quantity", 1,
                                "unitPrice", "14.99"
                        )
                )
        );

        given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(placeCommand)
                .when()
                .post("/api/v2/orders/commands/place")
                .then()
                .statusCode(201)
                .body("orderId", notNullValue());
    }

    // =========================================================
    // Validation tests
    // =========================================================

    @Test
    @DisplayName("POST /api/v1/orders — missing required fields returns 400")
    void createOrder_withMissingFields_returns400() {
        Map<String, Object> invalidRequest = Map.of("customerId", 1001L);

        given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} — non-existent order returns 404")
    void getOrder_withNonExistentId_returns404() {
        given()
                .get("/api/v1/orders/999999999")
                .then()
                .statusCode(404);
    }

    // =========================================================
    // OpenAPI documentation test
    // =========================================================

    @Test
    @DisplayName("GET /api-docs — OpenAPI spec is accessible")
    void openApiSpec_isAccessible() {
        given()
                .when()
                .get("/api-docs")
                .then()
                .statusCode(200)
                .body("info.title", containsString("Order Service"));
    }

    @Test
    @DisplayName("GET /swagger-ui.html — Swagger UI is accessible")
    void swaggerUi_isAccessible() {
        given()
                .when()
                .get("/swagger-ui.html")
                .then()
                .statusCode(in(List.of(200, 302)));
    }
}
