package com.pizzaflow.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when inventory reservation is released (e.g., order
 * cancelled).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReleasedEvent {
    private UUID orderId;
    private UUID restaurantId;
    private LocalDateTime releasedAt;
}
