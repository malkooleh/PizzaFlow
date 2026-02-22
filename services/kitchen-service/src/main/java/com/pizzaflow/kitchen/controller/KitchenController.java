package com.pizzaflow.kitchen.controller;

import com.pizzaflow.common.dto.ApiResponse;
import com.pizzaflow.kitchen.dto.KitchenOrderDTO;
import com.pizzaflow.kitchen.dto.QueueStatusDTO;
import com.pizzaflow.kitchen.dto.UpdateOrderStatusRequest;
import com.pizzaflow.kitchen.model.enums.KitchenOrderStatus;
import com.pizzaflow.kitchen.service.KitchenQueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenQueueService kitchenQueueService;

    /**
     * Get kitchen queue status for a restaurant.
     */
    @GetMapping("/queue/{restaurantId}")
    public ResponseEntity<ApiResponse<QueueStatusDTO>> getQueueStatus(
            @PathVariable Long restaurantId) {
        log.info("REST: Getting queue status for restaurant: {}", restaurantId);
        QueueStatusDTO status = kitchenQueueService.getQueueStatus(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * Get a specific kitchen order.
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<KitchenOrderDTO>> getKitchenOrder(
            @PathVariable Long orderId) {
        log.info("REST: Getting kitchen order: {}", orderId);
        KitchenOrderDTO order = kitchenQueueService.getKitchenOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * Update order status (start preparing, mark ready, or mark picked up).
     */
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<KitchenOrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("REST: Updating order {} status to {}", orderId, request.getStatus());

        KitchenOrderDTO result = switch (request.getStatus()) {
            case PREPARING -> kitchenQueueService.startPreparing(orderId, request.getAssignedStation());
            case READY -> kitchenQueueService.markReady(orderId);
            case PICKED_UP -> kitchenQueueService.markPickedUp(orderId);
            default -> throw new IllegalArgumentException("Invalid status transition: " + request.getStatus());
        };

        return ResponseEntity.ok(ApiResponse.success(result, "Order status updated"));
    }

    /**
     * Start preparing an order (shortcut endpoint).
     */
    @PostMapping("/orders/{orderId}/start")
    public ResponseEntity<ApiResponse<KitchenOrderDTO>> startPreparing(
            @PathVariable Long orderId,
            @RequestParam(required = false) String station) {
        log.info("REST: Starting preparation for order: {}, station: {}", orderId, station);
        KitchenOrderDTO result = kitchenQueueService.startPreparing(orderId, station);
        return ResponseEntity.ok(ApiResponse.success(result, "Order preparation started"));
    }

    /**
     * Mark order as ready (shortcut endpoint).
     */
    @PostMapping("/orders/{orderId}/ready")
    public ResponseEntity<ApiResponse<KitchenOrderDTO>> markReady(
            @PathVariable Long orderId) {
        log.info("REST: Marking order as ready: {}", orderId);
        KitchenOrderDTO result = kitchenQueueService.markReady(orderId);
        return ResponseEntity.ok(ApiResponse.success(result, "Order marked as ready"));
    }

    /**
     * Mark order as picked up (shortcut endpoint).
     */
    @PostMapping("/orders/{orderId}/pickup")
    public ResponseEntity<ApiResponse<KitchenOrderDTO>> markPickedUp(
            @PathVariable Long orderId) {
        log.info("REST: Marking order as picked up: {}", orderId);
        KitchenOrderDTO result = kitchenQueueService.markPickedUp(orderId);
        return ResponseEntity.ok(ApiResponse.success(result, "Order picked up"));
    }
}
