package com.pizzaflow.kitchen.dto;

import com.pizzaflow.kitchen.model.enums.KitchenOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for WebSocket updates to Kitchen Display System.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {

    private String updateType; // NEW_ORDER, STATUS_CHANGE, QUEUE_UPDATE, ORDER_REMOVED
    private Long orderId;
    private String orderNumber;
    private Long restaurantId;
    private KitchenOrderStatus status;
    private KitchenOrderStatus previousStatus;
    private Integer queuePosition;
    private Integer estimatedPrepTimeMinutes;
    private LocalDateTime timestamp;
}
