package com.pizzaflow.delivery.controller;

import com.pizzaflow.delivery.dto.*;
import com.pizzaflow.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
@Tag(name = "Deliveries", description = "Delivery creation, state transitions, and real-time location tracking")
public class DeliveryController {

    private static final Logger log = LoggerFactory.getLogger(DeliveryController.class);

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // ========== Delivery CRUD ==========

    @Operation(summary = "Create a delivery", description = "Creates a delivery record for an order that is ready for pickup")
    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(@Valid @RequestBody CreateDeliveryRequest request) {
        log.info("Creating delivery for order {}", request.orderId());
        DeliveryResponse response = deliveryService.createDelivery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get delivery by ID")
    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable UUID deliveryId) {
        DeliveryResponse response = deliveryService.getDelivery(deliveryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get delivery by order ID")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrder(@PathVariable UUID orderId) {
        DeliveryResponse response = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all active deliveries")
    @GetMapping("/active")
    public ResponseEntity<Page<DeliveryResponse>> getActiveDeliveries(
            @Parameter(hidden = true) @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DeliveryResponse> deliveries = deliveryService.getActiveDeliveries(pageable);
        return ResponseEntity.ok(deliveries);
    }

    // ========== Delivery State Transitions ==========

    @Operation(summary = "Assign a courier to a delivery")
    @PostMapping("/{deliveryId}/assign")
    public ResponseEntity<DeliveryResponse> assignCourier(
            @PathVariable UUID deliveryId,
            @RequestParam UUID courierId) {
        log.info("Assigning courier {} to delivery {}", courierId, deliveryId);
        DeliveryResponse response = deliveryService.assignCourier(deliveryId, courierId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark delivery as picked up from restaurant")
    @PostMapping("/{deliveryId}/pickup")
    public ResponseEntity<DeliveryResponse> markPickedUp(@PathVariable UUID deliveryId) {
        log.info("Marking delivery {} as picked up", deliveryId);
        DeliveryResponse response = deliveryService.markPickedUp(deliveryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark delivery as in transit")
    @PostMapping("/{deliveryId}/in-transit")
    public ResponseEntity<DeliveryResponse> markInTransit(@PathVariable UUID deliveryId) {
        log.info("Marking delivery {} as in transit", deliveryId);
        DeliveryResponse response = deliveryService.markInTransit(deliveryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark courier arrived at destination")
    @PostMapping("/{deliveryId}/arrived")
    public ResponseEntity<DeliveryResponse> markArrived(@PathVariable UUID deliveryId) {
        log.info("Marking courier arrived for delivery {}", deliveryId);
        DeliveryResponse response = deliveryService.markArrived(deliveryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Complete a delivery")
    @PostMapping("/{deliveryId}/complete")
    public ResponseEntity<DeliveryResponse> completeDelivery(
            @PathVariable UUID deliveryId,
            @RequestParam(required = false) String notes) {
        log.info("Completing delivery {}", deliveryId);
        DeliveryResponse response = deliveryService.completeDelivery(deliveryId, notes);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark a delivery as failed")
    @PostMapping("/{deliveryId}/fail")
    public ResponseEntity<DeliveryResponse> failDelivery(
            @PathVariable UUID deliveryId,
            @RequestParam String reason) {
        log.info("Marking delivery {} as failed: {}", deliveryId, reason);
        DeliveryResponse response = deliveryService.failDelivery(deliveryId, reason);
        return ResponseEntity.ok(response);
    }

    // ========== Tracking ==========

    @Operation(summary = "Track a delivery", description = "Returns current location, status, and ETA for a delivery")
    @GetMapping("/{deliveryId}/track")
    public ResponseEntity<TrackingInfo> trackDelivery(@PathVariable UUID deliveryId) {
        TrackingInfo tracking = deliveryService.getTrackingInfo(deliveryId);
        return ResponseEntity.ok(tracking);
    }

    @Operation(summary = "Update courier location for a delivery")
    @PostMapping("/{deliveryId}/location")
    public ResponseEntity<Void> updateLocation(
            @PathVariable UUID deliveryId,
            @Valid @RequestBody LocationUpdateRequest request) {
        deliveryService.updateCourierLocation(deliveryId, request);
        return ResponseEntity.ok().build();
    }

    // ========== Health ==========

    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Delivery service is running");
    }
}
