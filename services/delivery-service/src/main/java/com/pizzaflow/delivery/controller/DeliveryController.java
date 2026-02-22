package com.pizzaflow.delivery.controller;

import com.pizzaflow.delivery.dto.*;
import com.pizzaflow.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private static final Logger log = LoggerFactory.getLogger(DeliveryController.class);

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // ========== Delivery CRUD ==========

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(@Valid @RequestBody CreateDeliveryRequest request) {
        log.info("Creating delivery for order {}", request.orderId());
        DeliveryResponse response = deliveryService.createDelivery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable UUID deliveryId) {
        DeliveryResponse response = deliveryService.getDelivery(deliveryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrder(@PathVariable UUID orderId) {
        DeliveryResponse response = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<DeliveryResponse>> getActiveDeliveries() {
        List<DeliveryResponse> deliveries = deliveryService.getActiveDeliveries();
        return ResponseEntity.ok(deliveries);
    }

    // ========== Delivery State Transitions ==========

    @PostMapping("/{deliveryId}/assign")
    public ResponseEntity<DeliveryResponse> assignCourier(
        @PathVariable UUID deliveryId,
        @RequestParam UUID courierId
    ) {
        log.info("Assigning courier {} to delivery {}", courierId, deliveryId);
        DeliveryResponse response = deliveryService.assignCourier(deliveryId, courierId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deliveryId}/pickup")
    public ResponseEntity<DeliveryResponse> markPickedUp(@PathVariable UUID deliveryId) {
        log.info("Marking delivery {} as picked up", deliveryId);
        DeliveryResponse response = deliveryService.markPickedUp(deliveryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deliveryId}/in-transit")
    public ResponseEntity<DeliveryResponse> markInTransit(@PathVariable UUID deliveryId) {
        log.info("Marking delivery {} as in transit", deliveryId);
        DeliveryResponse response = deliveryService.markInTransit(deliveryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deliveryId}/arrived")
    public ResponseEntity<DeliveryResponse> markArrived(@PathVariable UUID deliveryId) {
        log.info("Marking courier arrived for delivery {}", deliveryId);
        DeliveryResponse response = deliveryService.markArrived(deliveryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deliveryId}/complete")
    public ResponseEntity<DeliveryResponse> completeDelivery(
        @PathVariable UUID deliveryId,
        @RequestParam(required = false) String notes
    ) {
        log.info("Completing delivery {}", deliveryId);
        DeliveryResponse response = deliveryService.completeDelivery(deliveryId, notes);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deliveryId}/fail")
    public ResponseEntity<DeliveryResponse> failDelivery(
        @PathVariable UUID deliveryId,
        @RequestParam String reason
    ) {
        log.info("Marking delivery {} as failed: {}", deliveryId, reason);
        DeliveryResponse response = deliveryService.failDelivery(deliveryId, reason);
        return ResponseEntity.ok(response);
    }

    // ========== Tracking ==========

    @GetMapping("/{deliveryId}/track")
    public ResponseEntity<TrackingInfo> trackDelivery(@PathVariable UUID deliveryId) {
        TrackingInfo tracking = deliveryService.getTrackingInfo(deliveryId);
        return ResponseEntity.ok(tracking);
    }

    @PostMapping("/{deliveryId}/location")
    public ResponseEntity<Void> updateLocation(
        @PathVariable UUID deliveryId,
        @Valid @RequestBody LocationUpdateRequest request
    ) {
        deliveryService.updateCourierLocation(deliveryId, request);
        return ResponseEntity.ok().build();
    }

    // ========== Health ==========

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Delivery service is running");
    }
}
