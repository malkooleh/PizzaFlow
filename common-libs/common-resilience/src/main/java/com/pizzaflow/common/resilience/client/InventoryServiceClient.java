package com.pizzaflow.common.resilience.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Feign client for Inventory Service.
 */
@FeignClient(
    name = "inventory-service",
    fallback = InventoryServiceClient.InventoryServiceFallback.class,
    configuration = com.pizzaflow.common.resilience.config.FeignClientConfig.class
)
public interface InventoryServiceClient {

    @GetMapping("/api/v1/inventory/stock/{ingredientId}")
    StockResponse getStock(@PathVariable("ingredientId") UUID ingredientId);

    @PostMapping("/api/v1/inventory/check-availability")
    AvailabilityResponse checkAvailability(@RequestBody AvailabilityRequest request);

    @PostMapping("/api/v1/inventory/reserve")
    ReservationResponse reserveStock(@RequestBody ReservationRequest request);

    @PostMapping("/api/v1/inventory/release")
    void releaseReservation(@RequestBody UUID reservationId);

    @PostMapping("/api/v1/inventory/consume")
    void consumeReservation(@RequestBody UUID reservationId);

    // DTO classes
    record StockResponse(
        UUID ingredientId,
        String ingredientName,
        int currentQuantity,
        int reservedQuantity,
        int availableQuantity,
        int lowStockThreshold,
        boolean lowStock
    ) {}

    record AvailabilityRequest(
        UUID restaurantId,
        List<IngredientQuantity> ingredients
    ) {}

    record IngredientQuantity(
        UUID ingredientId,
        int quantity
    ) {}

    record AvailabilityResponse(
        boolean available,
        List<UnavailableItem> unavailableItems
    ) {}

    record UnavailableItem(
        UUID ingredientId,
        String name,
        int requested,
        int available
    ) {}

    record ReservationRequest(
        UUID orderId,
        UUID restaurantId,
        List<IngredientQuantity> ingredients
    ) {}

    record ReservationResponse(
        UUID reservationId,
        boolean success,
        String message,
        Map<UUID, Integer> reservedQuantities
    ) {}

    // Fallback implementation
    class InventoryServiceFallback implements InventoryServiceClient {
        
        @Override
        public StockResponse getStock(UUID ingredientId) {
            return new StockResponse(
                ingredientId,
                "Unknown",
                0, 0, 0, 0,
                true
            );
        }

        @Override
        public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
            return new AvailabilityResponse(false, List.of());
        }

        @Override
        public ReservationResponse reserveStock(ReservationRequest request) {
            return new ReservationResponse(
                null, 
                false, 
                "Inventory service unavailable", 
                Map.of()
            );
        }

        @Override
        public void releaseReservation(UUID reservationId) {
            // No-op fallback
        }

        @Override
        public void consumeReservation(UUID reservationId) {
            // No-op fallback
        }
    }
}
