package com.pizzaflow.order.dto;

import com.pizzaflow.order.model.enums.OrderStatus;
import com.pizzaflow.order.model.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private Long customerId;
    private Long restaurantId;
    private OrderType orderType;
    private OrderStatus status;
    private LocalDateTime scheduledTime;
    private String tableNumber;
    private Long reservationId;
    private String deliveryAddress;
    private String specialInstructions;
    private List<OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private String menuItemId;
        private String menuItemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String customizations;
        private String specialInstructions;
    }
}
