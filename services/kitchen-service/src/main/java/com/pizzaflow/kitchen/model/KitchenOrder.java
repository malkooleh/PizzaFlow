package com.pizzaflow.kitchen.model;

import com.pizzaflow.kitchen.model.enums.KitchenOrderStatus;
import com.pizzaflow.kitchen.model.enums.OrderPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "kitchen_order", timeToLive = 86400) // 24 hours TTL
public class KitchenOrder implements Serializable {

    @Id
    private String id;

    @Indexed
    private Long orderId;

    private String orderNumber;

    @Indexed
    private Long restaurantId;

    private Long customerId;

    private String orderType;

    @Indexed
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
