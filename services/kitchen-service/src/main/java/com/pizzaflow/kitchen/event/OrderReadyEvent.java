package com.pizzaflow.kitchen.event;

import com.pizzaflow.common.dto.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Event published when order is ready for pickup/delivery.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderReadyEvent extends BaseEvent {

    private Long orderId;
    private String orderNumber;
    private Long restaurantId;
    private Long customerId;
    private String orderType;
    private LocalDateTime readyAt;
    private Integer actualPrepTimeMinutes;
}
