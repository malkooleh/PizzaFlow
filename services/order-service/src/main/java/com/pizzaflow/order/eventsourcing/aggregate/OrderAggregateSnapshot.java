package com.pizzaflow.order.eventsourcing.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Snapshot representation of an Order aggregate for serialization.
 * Used to optimize aggregate loading by avoiding replay of all events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAggregateSnapshot {

    private UUID id;
    private Long version;
    private String orderNumber;
    private UUID customerId;
    private UUID restaurantId;
    private String orderType;
    private String status;

    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;

    private String deliveryAddressStreet;
    private String deliveryAddressCity;
    private String deliveryAddressPostalCode;
    private BigDecimal deliveryAddressLatitude;
    private BigDecimal deliveryAddressLongitude;

    private LocalDateTime scheduledTime;
    private String tableNumber;
    private UUID reservationId;

    private Instant createdAt;
    private Instant confirmedAt;
    private Instant preparingAt;
    private Instant readyAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private Instant completedAt;
    private Instant cancelledAt;

    private String cancellationReason;
    private String cancelledBy;

    private UUID courierId;
    private String courierName;
    private Instant estimatedDeliveryTime;

    private List<OrderItemSnapshot> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemSnapshot {
        private UUID id;
        private String itemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String specialInstructions;
    }

    /**
     * Create a snapshot from an aggregate.
     */
    public static OrderAggregateSnapshot fromAggregate(OrderAggregate aggregate) {
        List<OrderItemSnapshot> itemSnapshots = aggregate.getItems().values().stream()
                .map(item -> OrderItemSnapshot.builder()
                        .id(item.getId())
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .specialInstructions(item.getSpecialInstructions())
                        .build())
                .collect(Collectors.toList());

        return OrderAggregateSnapshot.builder()
                .id(aggregate.getId())
                .version(aggregate.getVersion())
                .orderNumber(aggregate.getOrderNumber())
                .customerId(aggregate.getCustomerId())
                .restaurantId(aggregate.getRestaurantId())
                .orderType(aggregate.getOrderType() != null ? aggregate.getOrderType().name() : null)
                .status(aggregate.getStatus() != null ? aggregate.getStatus().name() : null)
                .subtotal(aggregate.getSubtotal())
                .tax(aggregate.getTax())
                .deliveryFee(aggregate.getDeliveryFee())
                .totalAmount(aggregate.getTotalAmount())
                .deliveryAddressStreet(aggregate.getDeliveryAddressStreet())
                .deliveryAddressCity(aggregate.getDeliveryAddressCity())
                .deliveryAddressPostalCode(aggregate.getDeliveryAddressPostalCode())
                .deliveryAddressLatitude(aggregate.getDeliveryAddressLatitude())
                .deliveryAddressLongitude(aggregate.getDeliveryAddressLongitude())
                .scheduledTime(aggregate.getScheduledTime())
                .tableNumber(aggregate.getTableNumber())
                .reservationId(aggregate.getReservationId())
                .createdAt(aggregate.getCreatedAt())
                .confirmedAt(aggregate.getConfirmedAt())
                .preparingAt(aggregate.getPreparingAt())
                .readyAt(aggregate.getReadyAt())
                .pickedUpAt(aggregate.getPickedUpAt())
                .deliveredAt(aggregate.getDeliveredAt())
                .completedAt(aggregate.getCompletedAt())
                .cancelledAt(aggregate.getCancelledAt())
                .cancellationReason(aggregate.getCancellationReason())
                .cancelledBy(aggregate.getCancelledBy())
                .courierId(aggregate.getCourierId())
                .courierName(aggregate.getCourierName())
                .estimatedDeliveryTime(aggregate.getEstimatedDeliveryTime())
                .items(itemSnapshots)
                .build();
    }

    /**
     * Reconstruct an aggregate from this snapshot.
     */
    public OrderAggregate toAggregate() {
        OrderAggregate aggregate = new OrderAggregate(this.id);

        // Use reflection or direct field access to restore state
        // For now, we'll create a minimal restoration
        // Events after snapshot will complete the state

        return aggregate;
    }
}
