package com.pizzaflow.order.eventsourcing.aggregate;

import com.pizzaflow.order.model.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Command to place a new order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderCommand {

    private UUID customerId;
    private UUID restaurantId;
    private OrderType orderType;
    private LocalDateTime scheduledTime;
    private String tableNumber;
    private UUID reservationId;
    private String deliveryAddressStreet;
    private String deliveryAddressCity;
    private String deliveryAddressPostalCode;
    private BigDecimal deliveryAddressLatitude;
    private BigDecimal deliveryAddressLongitude;
    private List<OrderItemCommand> items;
    private String triggeredBy;
    private String correlationId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemCommand {
        private String itemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private String specialInstructions;
    }
}
