package com.pizzaflow.tests.booking;

import com.pizzaflow.tests.infrastructure.BaseApiIT;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * End-to-end integration tests for the Booking Service.
 *
 * <p><strong>Prerequisites:</strong></p>
 * <ul>
 *   <li>Booking Service running on {@code BOOKING_SERVICE_URL} (default: {@code http://localhost:8086})</li>
 *   <li>PostgreSQL with the booking schema initialized</li>
 *   <li>Kafka for notification events</li>
 * </ul>
 *
 * <p><strong>To run:</strong></p>
 * <pre>
 *   docker-compose -f infrastructure/docker/docker-compose.yml up -d
 *   mvn verify -pl api-tests -Pintegration-tests
 * </pre>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingApiIT extends BaseApiIT {

    private static final String BASE_URL =
            System.getProperty("booking.service.url", "http://localhost:8086");

    /** Shared restaurant ID — must exist in the test database */
    private static final String TEST_RESTAURANT_ID = "550e8400-e29b-41d4-a716-446655440010";
    private static final String TEST_CUSTOMER_ID = "550e8400-e29b-41d4-a716-446655440020";

    /** Shared state: booking created in test 1, reused in subsequent tests */
    private static String createdBookingId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = "";
    }

    // =========================================================
    // Restaurant Endpoints
    // =========================================================

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("GET /api/v1/restaurants — list active restaurants")
    void listRestaurants_returnsActiveRestaurants() {
        getOk("/api/v1/restaurants")
                .body("$", not(empty()));
    }

    // =========================================================
    // Availability Check
    // =========================================================

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("GET /api/v1/bookings/availability — check table availability")
    void checkAvailability_returnsTimeSlots() {
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);

        given()
                .queryParam("restaurantId", TEST_RESTAURANT_ID)
                .queryParam("date", tomorrow)
                .queryParam("partySize", 2)
                .when()
                .get("/api/v1/bookings/availability")
                .then()
                .statusCode(200)
                .body("date", equalTo(tomorrow))
                .body("restaurantId", equalTo(TEST_RESTAURANT_ID));
    }

    // =========================================================
    // Happy-path flow: Create → Confirm → Seat → Complete
    // =========================================================

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("POST /api/v1/bookings — create a new reservation")
    void createBooking_returnsCreatedBooking() {
        String bookingDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);

        Map<String, Object> bookingRequest = Map.of(
                "customerId", TEST_CUSTOMER_ID,
                "restaurantId", TEST_RESTAURANT_ID,
                "partySize", 2,
                "bookingDate", bookingDate,
                "bookingTime", "19:00",
                "specialRequests", "Window table preferred"
        );

        ValidatableResponse response = postCreated("/api/v1/bookings", bookingRequest);

        createdBookingId = response
                .body("id", notNullValue())
                .body("status", equalTo("PENDING"))
                .body("customerId", equalTo(TEST_CUSTOMER_ID))
                .body("partySize", equalTo(2))
                .extract().jsonPath().getString("id");

        Assertions.assertNotNull(createdBookingId, "Booking ID must not be null after creation");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("GET /api/v1/bookings/{id} — retrieve booking by ID")
    void getBooking_returnsBookingDetails() {
        Assumptions.assumeTrue(createdBookingId != null, "Requires booking created in test 3");

        getOk("/api/v1/bookings/{id}", createdBookingId)
                .body("id", equalTo(createdBookingId))
                .body("status", equalTo("PENDING"));
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("POST /api/v1/bookings/{id}/confirm — confirm the booking")
    void confirmBooking_transitionsToConfirmed() {
        Assumptions.assumeTrue(createdBookingId != null, "Requires booking created in test 3");

        given()
                .when()
                .post("/api/v1/bookings/{id}/confirm", createdBookingId)
                .then()
                .statusCode(200)
                .body("status", equalTo("CONFIRMED"));
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("GET /api/v1/bookings/customer/{customerId} — list customer bookings")
    void getCustomerBookings_includesNewBooking() {
        Assumptions.assumeTrue(createdBookingId != null, "Requires booking created in test 3");

        getOk("/api/v1/bookings/customer/{customerId}", TEST_CUSTOMER_ID)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("find { it.id == '" + createdBookingId + "' }", notNullValue());
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("POST /api/v1/bookings/{id}/seat — seat guests")
    void seatGuests_transitionsToSeated() {
        Assumptions.assumeTrue(createdBookingId != null, "Requires booking created in test 3");

        given()
                .when()
                .post("/api/v1/bookings/{id}/seat", createdBookingId)
                .then()
                .statusCode(200)
                .body("status", equalTo("SEATED"));
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("POST /api/v1/bookings/{id}/complete — complete the booking")
    void completeBooking_transitionsToCompleted() {
        Assumptions.assumeTrue(createdBookingId != null, "Requires booking created in test 3");

        given()
                .when()
                .post("/api/v1/bookings/{id}/complete", createdBookingId)
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"));
    }

    // =========================================================
    // Cancel flow
    // =========================================================

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("POST /api/v1/bookings/{id}/cancel — cancel a separate booking")
    void cancelBooking_transitionsToCancelled() {
        String bookingDate = LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_DATE);

        Map<String, Object> newBooking = Map.of(
                "customerId", TEST_CUSTOMER_ID,
                "restaurantId", TEST_RESTAURANT_ID,
                "partySize", 4,
                "bookingDate", bookingDate,
                "bookingTime", "20:00"
        );

        String bookingToCancel = postCreated("/api/v1/bookings", newBooking)
                .extract().jsonPath().getString("id");

        given()
                .queryParam("reason", "Test cancellation")
                .when()
                .post("/api/v1/bookings/{id}/cancel", bookingToCancel)
                .then()
                .statusCode(200)
                .body("status", equalTo("CANCELLED"));
    }

    // =========================================================
    // Today's bookings (restaurant view)
    // =========================================================

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("GET /api/v1/bookings/restaurant/{restaurantId}/today — today's bookings")
    void getTodaysBookings_returnsList() {
        getOk("/api/v1/bookings/restaurant/{restaurantId}/today", TEST_RESTAURANT_ID)
                .body("$", instanceOf(List.class));
    }

    // =========================================================
    // Validation tests
    // =========================================================

    @Test
    @DisplayName("POST /api/v1/bookings — past booking date returns 400")
    void createBooking_withPastDate_returns400() {
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE);

        Map<String, Object> invalidBooking = Map.of(
                "customerId", TEST_CUSTOMER_ID,
                "restaurantId", TEST_RESTAURANT_ID,
                "partySize", 2,
                "bookingDate", yesterday,
                "bookingTime", "18:00"
        );

        given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(invalidBooking)
                .when()
                .post("/api/v1/bookings")
                .then()
                .statusCode(in(List.of(400, 422)));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} — non-existent booking returns 404")
    void getBooking_nonExistent_returns404() {
        given()
                .get("/api/v1/bookings/{id}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    // =========================================================
    // OpenAPI documentation
    // =========================================================

    @Test
    @DisplayName("GET /api-docs — OpenAPI spec is accessible")
    void openApiSpec_isAccessible() {
        given()
                .when()
                .get("/api-docs")
                .then()
                .statusCode(200)
                .body("info.title", containsString("Booking"));
    }
}
