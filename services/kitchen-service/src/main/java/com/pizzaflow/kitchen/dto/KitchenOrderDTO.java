package com.pizzaflow.kitchen.dto;

import com.pizzaflow.kitchen.model.KitchenOrderItem;
import com.pizzaflow.kitchen.model.enums.KitchenOrderStatus;
import com.pizzaflow.kitchen.model.enums.OrderPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderDTO {

    private String id;
    private Long orderId;
    private String orderNumber;
    private Long restaurantId;
    private String orderType;
    private KitchenOrderStatus status;
    private OrderPriority priority;
    private List<KitchenOrderItem> items;
    private Integer estimatedPrepTimeMinutes;
    private LocalDateTime scheduledTime;
    private LocalDateTime receivedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String specialInstructions;
    private Integer queuePosition;
    private String assignedStation;
}
