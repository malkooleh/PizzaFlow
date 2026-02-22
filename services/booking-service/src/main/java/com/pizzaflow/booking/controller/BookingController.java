package com.pizzaflow.booking.controller;

import com.pizzaflow.booking.dto.*;
import com.pizzaflow.booking.service.AvailabilityService;
import com.pizzaflow.booking.service.BookingService;
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
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final AvailabilityService availabilityService;

    public BookingController(BookingService bookingService, AvailabilityService availabilityService) {
        this.bookingService = bookingService;
        this.availabilityService = availabilityService;
    }

    // ========== Availability Endpoints ==========

    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
        @RequestParam UUID restaurantId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(defaultValue = "2") int partySize
    ) {
        log.info("Checking availability for restaurant {} on {} for {} guests",
            restaurantId, date, partySize);
        AvailabilityResponse availability = availabilityService.getAvailability(
            restaurantId, date, partySize
        );
        return ResponseEntity.ok(availability);
    }

    // ========== Booking CRUD Endpoints ==========

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
        @Valid @RequestBody BookingRequest request
    ) {
        log.info("Creating booking for customer {} at restaurant {}",
            request.customerId(), request.restaurantId());
        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable UUID bookingId) {
        BookingResponse booking = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/number/{bookingNumber}")
    public ResponseEntity<BookingResponse> getBookingByNumber(@PathVariable String bookingNumber) {
        BookingResponse booking = bookingService.getBookingByNumber(bookingNumber);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BookingResponse>> getCustomerBookings(@PathVariable UUID customerId) {
        List<BookingResponse> bookings = bookingService.getCustomerBookings(customerId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/restaurant/{restaurantId}/today")
    public ResponseEntity<List<BookingResponse>> getTodaysBookings(@PathVariable UUID restaurantId) {
        List<BookingResponse> bookings = bookingService.getTodaysBookings(restaurantId);
        return ResponseEntity.ok(bookings);
    }

    // ========== Booking State Changes ==========

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable UUID bookingId) {
        log.info("Confirming booking {}", bookingId);
        BookingResponse booking = bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
        @PathVariable UUID bookingId,
        @RequestParam(required = false) String reason
    ) {
        log.info("Cancelling booking {}: {}", bookingId, reason);
        BookingResponse booking = bookingService.cancelBooking(bookingId, reason);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/seat")
    public ResponseEntity<BookingResponse> seatGuests(@PathVariable UUID bookingId) {
        log.info("Seating guests for booking {}", bookingId);
        BookingResponse booking = bookingService.seatGuests(bookingId);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable UUID bookingId) {
        log.info("Completing booking {}", bookingId);
        BookingResponse booking = bookingService.completeBooking(bookingId);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/no-show")
    public ResponseEntity<BookingResponse> markNoShow(@PathVariable UUID bookingId) {
        log.info("Marking no-show for booking {}", bookingId);
        BookingResponse booking = bookingService.markNoShow(bookingId);
        return ResponseEntity.ok(booking);
    }

    // ========== Pre-Order Integration ==========

    @PostMapping("/{bookingId}/link-order")
    public ResponseEntity<BookingResponse> linkPreOrder(
        @PathVariable UUID bookingId,
        @Valid @RequestBody LinkPreOrderRequest request
    ) {
        log.info("Linking pre-order {} to booking {}", request.preOrderId(), bookingId);
        BookingResponse booking = bookingService.linkPreOrder(bookingId, request.preOrderId());
        return ResponseEntity.ok(booking);
    }

    // ========== Health Check ==========

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Booking service is running");
    }
}
