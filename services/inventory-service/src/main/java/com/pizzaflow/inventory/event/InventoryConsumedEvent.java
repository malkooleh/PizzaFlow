package com.pizzaflow.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when inventory is consumed (ingredients used in order
 * preparation).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryConsumedEvent {
    private UUID orderId;
    private UUID restaurantId;
    private LocalDateTime consumedAt;
}
