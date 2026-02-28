package com.pizzaflow.booking.controller;

import com.pizzaflow.booking.dto.*;
import com.pizzaflow.booking.service.AvailabilityService;
import com.pizzaflow.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Table reservation, availability check, and booking lifecycle management")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final AvailabilityService availabilityService;

    public BookingController(BookingService bookingService, AvailabilityService availabilityService) {
        this.bookingService = bookingService;
        this.availabilityService = availabilityService;
    }

    // ========== Availability Endpoints ==========

    @Operation(summary = "Check table availability", description = "Returns available time slots for the given date and party size")
    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @RequestParam UUID restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "2") int partySize) {
        log.info("Checking availability for restaurant {} on {} for {} guests",
                restaurantId, date, partySize);
        AvailabilityResponse availability = availabilityService.getAvailability(
                restaurantId, date, partySize);
        return ResponseEntity.ok(availability);
    }

    // ========== Booking CRUD Endpoints ==========

    @Operation(summary = "Create a table reservation")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {
        log.info("Creating booking for customer {} at restaurant {}",
                request.customerId(), request.restaurantId());
        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @Operation(summary = "Get booking by ID")
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable UUID bookingId) {
        BookingResponse booking = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Get booking by booking number")
    @GetMapping("/number/{bookingNumber}")
    public ResponseEntity<BookingResponse> getBookingByNumber(@PathVariable String bookingNumber) {
        BookingResponse booking = bookingService.getBookingByNumber(bookingNumber);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Get all bookings for a customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BookingResponse>> getCustomerBookings(@PathVariable UUID customerId) {
        List<BookingResponse> bookings = bookingService.getCustomerBookings(customerId);
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Get today's bookings for a restaurant")
    @GetMapping("/restaurant/{restaurantId}/today")
    public ResponseEntity<List<BookingResponse>> getTodaysBookings(@PathVariable UUID restaurantId) {
        List<BookingResponse> bookings = bookingService.getTodaysBookings(restaurantId);
        return ResponseEntity.ok(bookings);
    }

    // ========== Booking State Changes ==========

    @Operation(summary = "Confirm a booking")
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable UUID bookingId) {
        log.info("Confirming booking {}", bookingId);
        BookingResponse booking = bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Cancel a booking")
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable UUID bookingId,
            @RequestParam(required = false) String reason) {
        log.info("Cancelling booking {}: {}", bookingId, reason);
        BookingResponse booking = bookingService.cancelBooking(bookingId, reason);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Seat guests for a booking")
    @PostMapping("/{bookingId}/seat")
    public ResponseEntity<BookingResponse> seatGuests(@PathVariable UUID bookingId) {
        log.info("Seating guests for booking {}", bookingId);
        BookingResponse booking = bookingService.seatGuests(bookingId);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Complete a booking")
    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable UUID bookingId) {
        log.info("Completing booking {}", bookingId);
        BookingResponse booking = bookingService.completeBooking(bookingId);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Mark booking as no-show")
    @PostMapping("/{bookingId}/no-show")
    public ResponseEntity<BookingResponse> markNoShow(@PathVariable UUID bookingId) {
        log.info("Marking no-show for booking {}", bookingId);
        BookingResponse booking = bookingService.markNoShow(bookingId);
        return ResponseEntity.ok(booking);
    }

    // ========== Pre-Order Integration ==========

    @Operation(summary = "Link a pre-order to a booking")
    @PostMapping("/{bookingId}/link-order")
    public ResponseEntity<BookingResponse> linkPreOrder(
            @PathVariable UUID bookingId,
            @Valid @RequestBody LinkPreOrderRequest request) {
        log.info("Linking pre-order {} to booking {}", request.preOrderId(), bookingId);
        BookingResponse booking = bookingService.linkPreOrder(bookingId, request.preOrderId());
        return ResponseEntity.ok(booking);
    }

    // ========== Health Check ==========

    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Booking service is running");
    }
}
