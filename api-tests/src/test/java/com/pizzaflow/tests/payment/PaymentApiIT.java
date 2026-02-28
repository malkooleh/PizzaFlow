package com.pizzaflow.tests.payment;

import com.pizzaflow.tests.infrastructure.BaseApiIT;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * End-to-end integration tests for the Payment Service.
 *
 * <p><strong>Prerequisites:</strong></p>
 * <ul>
 *   <li>Payment Service running on {@code payment.service.url} (default: {@code http://localhost:8082})</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentApiIT extends BaseApiIT {

    private static final String BASE_URL =
            System.getProperty("payment.service.url", "http://localhost:8082");

    private static UUID createdTransactionId;
    private static final Long TEST_ORDER_ID = 10001L;
    private static final Long TEST_CUSTOMER_ID = 1001L;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = "";
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("POST /api/v1/payments — process a payment")
    void processPayment_returnsApprovedTransaction() {
        Map<String, Object> paymentRequest = Map.of(
                "orderId", TEST_ORDER_ID,
                "customerId", TEST_CUSTOMER_ID,
                "amount", "25.98",
                "currency", "USD",
                "paymentMethod", Map.of(
                        "type", "CREDIT_CARD",
                        "cardNumber", "****-****-****-1111",
                        "expiryMonth", 12,
                        "expiryYear", 2026
                )
        );

        ValidatableResponse response = postCreated("/api/v1/payments", paymentRequest);

        createdTransactionId = UUID.fromString(response
                .body("success", equalTo(true))
                .body("data.transactionId", notNullValue())
                .body("data.status", equalTo("APPROVED"))
                .body("data.amount", equalTo(25.98f))
                .extract().jsonPath().getString("data.transactionId"));
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("GET /api/v1/payments/{transactionId} — retrieve payment by transaction ID")
    void getPayment_returnsPaymentDetails() {
        Assumptions.assumeTrue(createdTransactionId != null, "Requires payment created in test 1");

        getOk("/api/v1/payments/{id}", createdTransactionId)
                .body("data.transactionId", equalTo(createdTransactionId.toString()))
                .body("data.status", equalTo("APPROVED"));
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("GET /api/v1/payments/order/{orderId} — retrieve payment by order ID")
    void getPaymentByOrder_returnsPayment() {
        Assumptions.assumeTrue(createdTransactionId != null, "Requires payment created in test 1");

        getOk("/api/v1/payments/order/{orderId}", TEST_ORDER_ID)
                .body("data.transactionId", equalTo(createdTransactionId.toString()));
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("GET /api/v1/payments/customer/{customerId} — list customer payments")
    void getCustomerPayments_returnsList() {
        getOk("/api/v1/payments/customer/{customerId}", TEST_CUSTOMER_ID)
                .body("data", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("POST /api/v1/payments/refund — process refund")
    void processRefund_returnsApprovedRefund() {
        Assumptions.assumeTrue(createdTransactionId != null, "Requires payment created in test 1");

        Map<String, Object> refundRequest = Map.of(
                "transactionId", createdTransactionId.toString(),
                "amount", "25.98",
                "reason", "Integration test refund"
        );

        postCreated("/api/v1/payments/refund", refundRequest)
                .body("success", equalTo(true))
                .body("data.status", equalTo("REFUNDED"));
    }

    @Test
    @DisplayName("POST /api/v1/payments — declined card returns 200 with DECLINED status")
    void processPayment_withDeclinedCard_returnsDeclined() {
        Map<String, Object> declinedPayment = Map.of(
                "orderId", 99999L,
                "customerId", TEST_CUSTOMER_ID,
                "amount", "100.00",
                "currency", "USD",
                "paymentMethod", Map.of(
                        "type", "CREDIT_CARD",
                        "cardNumber", "****-****-****-0000",
                        "expiryMonth", 12,
                        "expiryYear", 2026
                )
        );

        // Declined is still a valid business outcome (not a server error)
        given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(declinedPayment)
                .when()
                .post("/api/v1/payments")
                .then()
                .statusCode(in(List.of(200, 201, 402)))
                .body("data.status", anyOf(equalTo("DECLINED"), equalTo("FAILED")));
    }

    @Test
    @DisplayName("GET /api/v1/payments/{transactionId} — non-existent transaction returns 404")
    void getPayment_nonExistent_returns404() {
        given()
                .get("/api/v1/payments/{id}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /api-docs — OpenAPI spec is accessible")
    void openApiSpec_isAccessible() {
        given()
                .when()
                .get("/api-docs")
                .then()
                .statusCode(200)
                .body("info.title", containsString("Payment"));
    }
}
