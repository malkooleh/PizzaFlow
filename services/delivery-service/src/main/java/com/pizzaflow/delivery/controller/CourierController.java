package com.pizzaflow.delivery.controller;

import com.pizzaflow.delivery.dto.CourierResponse;
import com.pizzaflow.delivery.dto.DeliveryResponse;
import com.pizzaflow.delivery.dto.LocationUpdateRequest;
import com.pizzaflow.delivery.model.enums.CourierStatus;
import com.pizzaflow.delivery.model.enums.DeliveryStatus;
import com.pizzaflow.delivery.service.CourierService;
import com.pizzaflow.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/couriers")
@Tag(name = "Couriers", description = "Courier registration, availability status, and location management")
public class CourierController {

    private static final Logger log = LoggerFactory.getLogger(CourierController.class);

    private final CourierService courierService;
    private final DeliveryService deliveryService;

    public CourierController(CourierService courierService, DeliveryService deliveryService) {
        this.courierService = courierService;
        this.deliveryService = deliveryService;
    }

    // ========== Courier Info ==========

    @Operation(summary = "Get courier profile by ID")
    @GetMapping("/{courierId}")
    public ResponseEntity<CourierResponse> getCourier(@PathVariable UUID courierId) {
        CourierResponse response = courierService.getCourier(courierId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get courier profile by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<CourierResponse> getCourierByUserId(@PathVariable UUID userId) {
        CourierResponse response = courierService.getCourierByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List all available couriers")
    @GetMapping("/available")
    public ResponseEntity<Page<CourierResponse>> getAvailableCouriers(
            @Parameter(hidden = true) @PageableDefault(size = 50) Pageable pageable) {
        Page<CourierResponse> couriers = courierService.getAvailableCouriers(pageable);
        return ResponseEntity.ok(couriers);
    }

    // ========== Courier Status Management ==========

    @Operation(summary = "Set courier as online", description = "Registers courier as available and sets their initial location")
    @PostMapping("/{courierId}/online")
    public ResponseEntity<CourierResponse> goOnline(
            @PathVariable UUID courierId,
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude) {
        log.info("Courier {} going online at ({}, {})", courierId, latitude, longitude);
        CourierResponse response = courierService.goOnline(courierId, latitude, longitude);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Set courier as offline")
    @PostMapping("/{courierId}/offline")
    public ResponseEntity<CourierResponse> goOffline(@PathVariable UUID courierId) {
        log.info("Courier {} going offline", courierId);
        CourierResponse response = courierService.goOffline(courierId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update courier status")
    @PostMapping("/{courierId}/status")
    public ResponseEntity<CourierResponse> updateStatus(
            @PathVariable UUID courierId,
            @RequestParam CourierStatus status) {
        log.info("Updating courier {} status to {}", courierId, status);
        CourierResponse response = courierService.updateStatus(courierId, status);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update courier GPS location")
    @PostMapping("/{courierId}/location")
    public ResponseEntity<CourierResponse> updateLocation(
            @PathVariable UUID courierId,
            @Valid @RequestBody LocationUpdateRequest request) {
        CourierResponse response = courierService.updateLocation(courierId, request);
        return ResponseEntity.ok(response);
    }

    // ========== Courier Deliveries ==========

    @Operation(summary = "Get all deliveries assigned to a courier")
    @GetMapping("/{courierId}/deliveries")
    public ResponseEntity<List<DeliveryResponse>> getCourierDeliveries(@PathVariable UUID courierId) {
        List<DeliveryResponse> deliveries = deliveryService.getCourierDeliveries(courierId);
        return ResponseEntity.ok(deliveries);
    }
}
