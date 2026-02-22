package com.pizzaflow.kitchen.event;

import com.pizzaflow.common.dto.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when kitchen starts preparing an order.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderPreparingEvent extends BaseEvent {

    private Long orderId;
    private String orderNumber;
    private Long restaurantId;
    private Integer estimatedPrepTimeMinutes;
    private String assignedStation;
}
