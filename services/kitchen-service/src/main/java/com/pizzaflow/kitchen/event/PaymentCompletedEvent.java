package com.pizzaflow.kitchen.event;

import com.pizzaflow.common.dto.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event received from Order Service when payment is completed.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentCompletedEvent extends BaseEvent {

    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private Long restaurantId;
    private BigDecimal totalAmount;
    private String orderType;
    private String currency;
    private List<OrderItemEvent> items;
    private String specialInstructions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private Long menuItemId;
        private String menuItemName;
        private Integer quantity;
        private String customizations;
        private String specialInstructions;
    }
}
