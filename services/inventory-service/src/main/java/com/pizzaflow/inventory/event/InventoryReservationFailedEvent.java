package com.pizzaflow.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event published when inventory reservation fails due to insufficient stock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationFailedEvent {
    private UUID orderId;
    private UUID restaurantId;
    private List<FailedItem> failedItems;
    private String reason;
    private LocalDateTime failedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedItem {
        private UUID ingredientId;
        private String ingredientName;
        private String requestedQuantity;
        private String availableQuantity;
    }
}
