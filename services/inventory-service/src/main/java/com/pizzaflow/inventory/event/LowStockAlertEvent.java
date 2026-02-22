package com.pizzaflow.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when stock level falls below minimum threshold.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertEvent {
    private UUID ingredientId;
    private String ingredientName;
    private UUID restaurantId;
    private String currentQuantity;
    private String minimumStockLevel;
    private String reorderQuantity;
    private LocalDateTime alertedAt;
}
