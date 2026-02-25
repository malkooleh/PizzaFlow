package com.pizzaflow.order.eventsourcing.controller;

import com.pizzaflow.order.eventsourcing.aggregate.PlaceOrderCommand;
import com.pizzaflow.order.eventsourcing.service.OrderCommandService;
import com.pizzaflow.order.model.enums.OrderType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for order commands (CQRS write side).
 * All modifying operations go through this controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/orders/commands")
@RequiredArgsConstructor
@Tag(name = "Order Commands", description = "CQRS command endpoints for order operations")
public class OrderCommandController {

    private final OrderCommandService commandService;

    @PostMapping("/place")
    @Operation(summary = "Place a new order")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        log.info("Received place order request for customer: {}", request.getCustomerId());

        PlaceOrderCommand command = PlaceOrderCommand.builder()
                .customerId(request.getCustomerId())
                .restaurantId(request.getRestaurantId())
                .orderType(request.getOrderType())
                .scheduledTime(request.getScheduledTime())
                .tableNumber(request.getTableNumber())
                .reservationId(request.getReservationId())
                .deliveryAddressStreet(request.getDeliveryAddressStreet())
                .deliveryAddressCity(request.getDeliveryAddressCity())
                .deliveryAddressPostalCode(request.getDeliveryAddressPostalCode())
                .deliveryAddressLatitude(request.getDeliveryAddressLatitude())
                .deliveryAddressLongitude(request.getDeliveryAddressLongitude())
                .items(request.getItems().stream()
                        .map(item -> PlaceOrderCommand.OrderItemCommand.builder()
                                .itemId(item.getItemId())
                                .itemName(item.getItemName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .specialInstructions(item.getSpecialInstructions())
                                .build())
                        .collect(Collectors.toList()))
                .triggeredBy(request.getTriggeredBy())
                .correlationId(request.getCorrelationId())
                .build();

        UUID orderId = commandService.placeOrder(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PlaceOrderResponse(orderId, "Order placed successfully"));
    }

    @PostMapping("/{orderId}/confirm")
    @Operation(summary = "Confirm an order after payment")
    public ResponseEntity<CommandResponse> confirmOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody ConfirmOrderRequest request) {
        log.info("Received confirm order request: {}", orderId);

        commandService.confirmOrder(
                orderId,
                request.getPaymentReference(),
                request.getEstimatedPrepTimeMinutes(),
                request.getTriggeredBy());

        return ResponseEntity.ok(new CommandResponse("Order confirmed successfully"));
    }

    @PostMapping("/{orderId}/prepare")
    @Operation(summary = "Start preparing an order")
    public ResponseEntity<CommandResponse> startPreparing(
            @PathVariable UUID orderId,
            @Valid @RequestBody StartPreparingRequest request) {
        log.info("Received start preparing request: {}", orderId);

        commandService.startPreparing(
                orderId,
                request.getKitchenStation(),
                request.getAssignedStaff(),
                request.getEstimatedCompletionTime(),
                request.getTriggeredBy());

        return ResponseEntity.ok(new CommandResponse("Order preparation started"));
    }

    @PostMapping("/{orderId}/ready")
    @Operation(summary = "Mark order as ready")
    public ResponseEntity<CommandResponse> markReady(
            @PathVariable UUID orderId,
            @Valid @RequestBody MarkReadyRequest request) {
        log.info("Received mark ready request: {}", orderId);

        commandService.markOrderReady(
                orderId,
                request.getPickupLocation(),
                request.getPackagingNotes(),
                request.getTriggeredBy());

        return ResponseEntity.ok(new CommandResponse("Order marked as ready"));
    }

    @PostMapping("/{orderId}/pickup")
    @Operation(summary = "Record order pickup by courier")
    public ResponseEntity<CommandResponse> pickUp(
            @PathVariable UUID orderId,
            @Valid @RequestBody PickUpRequest request) {
        log.info("Received pickup request: {}", orderId);

        commandService.pickUpOrder(
                orderId,
                request.getCourierId(),
                request.getCourierName(),
                request.getEstimatedDeliveryTime(),
                request.getTriggeredBy());

        return ResponseEntity.ok(new CommandResponse("Order picked up by courier"));
    }

    @PostMapping("/{orderId}/deliver")
    @Operation(summary = "Mark order as delivered")
    public ResponseEntity<CommandResponse> deliver(
            @PathVariable UUID orderId,
            @Valid @RequestBody DeliverRequest request) {
        log.info("Received deliver request: {}", orderId);

        commandService.deliverOrder(
                orderId,
                request.getReceivedBy(),
                request.getDeliveryConfirmation(),
                request.getNotes(),
                request.getTriggeredBy());

        return ResponseEntity.ok(new CommandResponse("Order delivered successfully"));
    }

    @PostMapping("/{orderId}/complete")
    @Operation(summary = "Complete an order")
    public ResponseEntity<CommandResponse> complete(
            @PathVariable UUID orderId,
            @Valid @RequestBody CompleteOrderRequest request) {
        log.info("Received complete order request: {}", orderId);

        commandService.completeOrder(
                orderId,
                request.getRating(),
                request.getFeedback(),
                request.getTriggeredBy());

        return ResponseEntity.ok(new CommandResponse("Order completed successfully"));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<CommandResponse> cancel(
            @PathVariable UUID orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        log.info("Received cancel order request: {}", orderId);

        commandService.cancelOrder(
                orderId,
                request.getReason(),
                request.getCancelledBy(),
                request.getRefundAmount(),
                request.getTriggeredBy());

        return ResponseEntity.ok(new CommandResponse("Order cancelled successfully"));
    }

    @PostMapping("/{orderId}/items")
    @Operation(summary = "Add an item to an order")
    public ResponseEntity<CommandResponse> addItem(
            @PathVariable UUID orderId,
            @Valid @RequestBody AddItemRequest request) {
        log.info("Received add item request for order: {}", orderId);

        commandService.addItem(
                orderId,
                request.getItemId(),
                request.getItemName(),
                request.getQuantity(),
                request.getUnitPrice(),
                request.getSpecialInstructions(),
                request.getTriggeredBy());

        return ResponseEntity.ok(new CommandResponse("Item added successfully"));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Remove an item from an order")
    public ResponseEntity<CommandResponse> removeItem(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String triggeredBy) {
        log.info("Received remove item request for order: {}, item: {}", orderId, itemId);

        commandService.removeItem(orderId, itemId, reason, triggeredBy);

        return ResponseEntity.ok(new CommandResponse("Item removed successfully"));
    }

    // ===========================================
    // Request/Response DTOs
    // ===========================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceOrderRequest {
        @NotNull
        private UUID customerId;
        @NotNull
        private UUID restaurantId;
        @NotNull
        private OrderType orderType;
        private LocalDateTime scheduledTime;
        private String tableNumber;
        private UUID reservationId;
        private String deliveryAddressStreet;
        private String deliveryAddressCity;
        private String deliveryAddressPostalCode;
        private BigDecimal deliveryAddressLatitude;
        private BigDecimal deliveryAddressLongitude;
        @NotNull
        private List<OrderItemRequest> items;
        private String triggeredBy;
        private String correlationId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private String itemId;
        @NotNull
        private String itemName;
        @NotNull
        private Integer quantity;
        @NotNull
        private BigDecimal unitPrice;
        private String specialInstructions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmOrderRequest {
        private String paymentReference;
        private Integer estimatedPrepTimeMinutes;
        private String triggeredBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartPreparingRequest {
        private String kitchenStation;
        private String assignedStaff;
        private Instant estimatedCompletionTime;
        private String triggeredBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarkReadyRequest {
        private String pickupLocation;
        private String packagingNotes;
        private String triggeredBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PickUpRequest {
        @NotNull
        private UUID courierId;
        private String courierName;
        private Instant estimatedDeliveryTime;
        private String triggeredBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliverRequest {
        private String receivedBy;
        private String deliveryConfirmation;
        private String notes;
        private String triggeredBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteOrderRequest {
        private Integer rating;
        private String feedback;
        private String triggeredBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelOrderRequest {
        @NotNull
        private String reason;
        @NotNull
        private String cancelledBy;
        private BigDecimal refundAmount;
        private String triggeredBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddItemRequest {
        private String itemId;
        @NotNull
        private String itemName;
        @NotNull
        private Integer quantity;
        @NotNull
        private BigDecimal unitPrice;
        private String specialInstructions;
        private String triggeredBy;
    }

    public record PlaceOrderResponse(UUID orderId, String message) {
    }

    public record CommandResponse(String message) {
    }
}
