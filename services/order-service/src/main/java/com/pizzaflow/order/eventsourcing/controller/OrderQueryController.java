package com.pizzaflow.order.eventsourcing.controller;

import com.pizzaflow.order.eventsourcing.readmodel.OrderItemReadModel;
import com.pizzaflow.order.eventsourcing.readmodel.OrderReadModel;
import com.pizzaflow.order.eventsourcing.service.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for order queries (CQRS read side).
 * All read-only operations go through this controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/orders/queries")
@RequiredArgsConstructor
@Tag(name = "Order Queries", description = "CQRS query endpoints for order read operations")
public class OrderQueryController {

    private final OrderQueryService queryService;

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable UUID orderId) {
        log.debug("Query: Get order by ID: {}", orderId);
        OrderReadModel order = queryService.getOrderWithItems(orderId);
        return ResponseEntity.ok(toDTO(order));
    }

    @GetMapping("/by-number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<OrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        log.debug("Query: Get order by number: {}", orderNumber);
        OrderReadModel order = queryService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(toDTO(order));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders for a customer")
    public ResponseEntity<List<OrderSummaryDTO>> getCustomerOrders(@PathVariable UUID customerId) {
        log.debug("Query: Get orders for customer: {}", customerId);
        List<OrderReadModel> orders = queryService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders.stream().map(this::toSummaryDTO).collect(Collectors.toList()));
    }

    @GetMapping("/customer/{customerId}/paged")
    @Operation(summary = "Get paginated orders for a customer")
    public ResponseEntity<Page<OrderSummaryDTO>> getCustomerOrdersPaged(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.debug("Query: Get paginated orders for customer: {}", customerId);
        Pageable pageable = PageRequest.of(page, size,
                Sort.Direction.valueOf(direction), sortBy);
        Page<OrderReadModel> orders = queryService.getCustomerOrders(customerId, pageable);
        return ResponseEntity.ok(orders.map(this::toSummaryDTO));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get orders for a restaurant")
    public ResponseEntity<Page<OrderSummaryDTO>> getRestaurantOrders(
            @PathVariable UUID restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Query: Get orders for restaurant: {}", restaurantId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderReadModel> orders = queryService.getRestaurantOrders(restaurantId, pageable);
        return ResponseEntity.ok(orders.map(this::toSummaryDTO));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status")
    public ResponseEntity<List<OrderSummaryDTO>> getOrdersByStatus(@PathVariable String status) {
        log.debug("Query: Get orders by status: {}", status);
        List<OrderReadModel> orders = queryService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders.stream().map(this::toSummaryDTO).collect(Collectors.toList()));
    }

    @GetMapping("/kitchen/{restaurantId}")
    @Operation(summary = "Get pending orders for kitchen display")
    public ResponseEntity<List<KitchenOrderDTO>> getKitchenOrders(@PathVariable UUID restaurantId) {
        log.debug("Query: Get kitchen orders for restaurant: {}", restaurantId);
        List<OrderReadModel> orders = queryService.getPendingOrdersForKitchen(restaurantId);
        return ResponseEntity.ok(orders.stream().map(this::toKitchenDTO).collect(Collectors.toList()));
    }

    @GetMapping("/ready/{restaurantId}")
    @Operation(summary = "Get orders ready for pickup/delivery")
    public ResponseEntity<List<OrderSummaryDTO>> getReadyOrders(@PathVariable UUID restaurantId) {
        log.debug("Query: Get ready orders for restaurant: {}", restaurantId);
        List<OrderReadModel> orders = queryService.getReadyOrders(restaurantId);
        return ResponseEntity.ok(orders.stream().map(this::toSummaryDTO).collect(Collectors.toList()));
    }

    @GetMapping("/courier/{courierId}/active")
    @Operation(summary = "Get active deliveries for a courier")
    public ResponseEntity<List<DeliveryOrderDTO>> getActiveDeliveries(@PathVariable UUID courierId) {
        log.debug("Query: Get active deliveries for courier: {}", courierId);
        List<OrderReadModel> orders = queryService.getActiveDeliveries(courierId);
        return ResponseEntity.ok(orders.stream().map(this::toDeliveryDTO).collect(Collectors.toList()));
    }

    @GetMapping("/scheduled")
    @Operation(summary = "Get scheduled orders within a time window")
    public ResponseEntity<List<OrderSummaryDTO>> getScheduledOrders(
            @RequestParam Instant from,
            @RequestParam Instant to) {
        log.debug("Query: Get scheduled orders from {} to {}", from, to);
        List<OrderReadModel> orders = queryService.getScheduledOrders(from, to);
        return ResponseEntity.ok(orders.stream().map(this::toSummaryDTO).collect(Collectors.toList()));
    }

    @GetMapping("/restaurant/{restaurantId}/stats")
    @Operation(summary = "Get order count by status for dashboard")
    public ResponseEntity<Map<String, Long>> getOrderStats(@PathVariable UUID restaurantId) {
        log.debug("Query: Get order stats for restaurant: {}", restaurantId);
        Map<String, Long> stats = queryService.getOrderCountByStatus(restaurantId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/search")
    @Operation(summary = "Search orders by order number pattern")
    public ResponseEntity<List<OrderSummaryDTO>> searchOrders(@RequestParam String q) {
        log.debug("Query: Search orders with pattern: {}", q);
        List<OrderReadModel> orders = queryService.searchOrders(q);
        return ResponseEntity.ok(orders.stream().map(this::toSummaryDTO).collect(Collectors.toList()));
    }

    @GetMapping("/stats/daily")
    @Operation(summary = "Get daily order statistics")
    public ResponseEntity<List<OrderQueryService.DailyOrderStats>> getDailyStats(
            @RequestParam Instant from) {
        log.debug("Query: Get daily stats from: {}", from);
        List<OrderQueryService.DailyOrderStats> stats = queryService.getDailyStats(from);
        return ResponseEntity.ok(stats);
    }

    // ===========================================
    // DTO Mapping Methods
    // ===========================================

    private OrderDTO toDTO(OrderReadModel order) {
        List<OrderItemDTO> items = order.getItems() != null
                ? order.getItems().stream().map(this::toItemDTO).collect(Collectors.toList())
                : List.of();

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(buildDeliveryAddress(order))
                .scheduledTime(order.getScheduledTime())
                .tableNumber(order.getTableNumber())
                .items(items)
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .preparingAt(order.getPreparingAt())
                .readyAt(order.getReadyAt())
                .pickedUpAt(order.getPickedUpAt())
                .deliveredAt(order.getDeliveredAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .courierName(order.getCourierName())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .build();
    }

    private OrderSummaryDTO toSummaryDTO(OrderReadModel order) {
        return OrderSummaryDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .scheduledTime(order.getScheduledTime())
                .build();
    }

    private KitchenOrderDTO toKitchenDTO(OrderReadModel order) {
        return KitchenOrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .tableNumber(order.getTableNumber())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .preparingAt(order.getPreparingAt())
                .itemCount(order.getItems() != null ? order.getItems().size() : 0)
                .build();
    }

    private DeliveryOrderDTO toDeliveryDTO(OrderReadModel order) {
        return DeliveryOrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .deliveryAddress(buildDeliveryAddress(order))
                .latitude(order.getDeliveryAddressLatitude())
                .longitude(order.getDeliveryAddressLongitude())
                .pickedUpAt(order.getPickedUpAt())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .build();
    }

    private OrderItemDTO toItemDTO(OrderItemReadModel item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .specialInstructions(item.getSpecialInstructions())
                .build();
    }

    private String buildDeliveryAddress(OrderReadModel order) {
        if (order.getDeliveryAddressStreet() == null) {
            return null;
        }
        return String.format("%s, %s %s",
                order.getDeliveryAddressStreet(),
                order.getDeliveryAddressCity(),
                order.getDeliveryAddressPostalCode());
    }

    // ===========================================
    // Response DTOs
    // ===========================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDTO {
        private UUID id;
        private String orderNumber;
        private UUID customerId;
        private UUID restaurantId;
        private String orderType;
        private String status;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal deliveryFee;
        private BigDecimal totalAmount;
        private String deliveryAddress;
        private Instant scheduledTime;
        private String tableNumber;
        private List<OrderItemDTO> items;
        private Instant createdAt;
        private Instant confirmedAt;
        private Instant preparingAt;
        private Instant readyAt;
        private Instant pickedUpAt;
        private Instant deliveredAt;
        private Instant completedAt;
        private Instant cancelledAt;
        private String cancellationReason;
        private String courierName;
        private Instant estimatedDeliveryTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummaryDTO {
        private UUID id;
        private String orderNumber;
        private String orderType;
        private String status;
        private BigDecimal totalAmount;
        private Instant createdAt;
        private Instant scheduledTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KitchenOrderDTO {
        private UUID id;
        private String orderNumber;
        private String orderType;
        private String status;
        private String tableNumber;
        private Instant createdAt;
        private Instant confirmedAt;
        private Instant preparingAt;
        private int itemCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryOrderDTO {
        private UUID id;
        private String orderNumber;
        private String deliveryAddress;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Instant pickedUpAt;
        private Instant estimatedDeliveryTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private UUID id;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String specialInstructions;
    }
}
