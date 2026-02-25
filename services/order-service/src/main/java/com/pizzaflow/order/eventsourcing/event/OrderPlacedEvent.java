package com.pizzaflow.order.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event raised when a new order is placed.
 * This is typically the first event in an order's lifecycle.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderPlacedEvent extends OrderDomainEvent {

    private String orderNumber;
    private UUID customerId;
    private UUID restaurantId;
    private String orderType;
    private LocalDateTime scheduledTime;
    private String tableNumber;
    private UUID reservationId;
    private String deliveryAddressStreet;
    private String deliveryAddressCity;
    private String deliveryAddressPostalCode;
    private BigDecimal deliveryAddressLatitude;
    private BigDecimal deliveryAddressLongitude;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private List<OrderItemData> items;

    @Override
    public String getEventType() {
        return "ORDER_PLACED";
    }

    /**
     * Nested class to hold order item data within the event
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class OrderItemData {
        private UUID itemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String specialInstructions;
    }
}
