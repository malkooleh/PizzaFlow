package com.pizzaflow.inventory.controller;

import com.pizzaflow.inventory.dto.*;
import com.pizzaflow.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory", description = "Ingredient stock, reservations, and consumption management using the Outbox pattern")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Reserve ingredients for an order.
     */
    @Operation(summary = "Reserve ingredients for an order", description = "Atomically reserves required ingredient quantities. Fails if stock is insufficient.")
    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserveIngredients(
            @Valid @RequestBody ReservationRequest request) {
        log.info("Reserve ingredients request for order: {}", request.getOrderId());
        ReservationResponse response = inventoryService.reserveIngredients(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Consume reserved ingredients when preparation starts.
     */
    @Operation(summary = "Consume reserved ingredients", description = "Permanently deducts reserved stock when kitchen starts preparing the order")
    @PostMapping("/reservations/{orderId}/consume")
    public ResponseEntity<Void> consumeReservedIngredients(@PathVariable UUID orderId) {
        log.info("Consume ingredients request for order: {}", orderId);
        inventoryService.consumeReservedIngredients(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Release reservations when order is cancelled.
     */
    @Operation(summary = "Release ingredient reservations", description = "Returns reserved stock to available inventory when an order is cancelled")
    @PostMapping("/reservations/{orderId}/release")
    public ResponseEntity<Void> releaseReservations(@PathVariable UUID orderId) {
        log.info("Release reservations request for order: {}", orderId);
        inventoryService.releaseReservations(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get stock levels for a restaurant.
     */
    @Operation(summary = "Get stock levels for a restaurant")
    @GetMapping("/stock/{restaurantId}")
    public ResponseEntity<List<StockLevelDTO>> getStockLevels(@PathVariable UUID restaurantId) {
        log.info("Get stock levels for restaurant: {}", restaurantId);
        return ResponseEntity.ok(inventoryService.getStockLevels(restaurantId));
    }

    /**
     * Get low stock items for a restaurant.
     */
    @Operation(summary = "Get low-stock ingredients", description = "Returns ingredients below their reorder threshold for the given restaurant")
    @GetMapping("/stock/{restaurantId}/low")
    public ResponseEntity<List<StockLevelDTO>> getLowStockItems(@PathVariable UUID restaurantId) {
        log.info("Get low stock items for restaurant: {}", restaurantId);
        return ResponseEntity.ok(inventoryService.getLowStockItems(restaurantId));
    }

    /**
     * Adjust stock level (restock).
     */
    @Operation(summary = "Adjust stock level", description = "Manually restock or adjust ingredient quantity at a restaurant")
    @PostMapping("/stock/adjust")
    public ResponseEntity<StockLevelDTO> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request) {
        log.info("Stock adjustment request for ingredient: {} at restaurant: {}",
                request.getIngredientId(), request.getRestaurantId());
        return ResponseEntity.ok(inventoryService.adjustStock(request));
    }

    /**
     * Get all active ingredients.
     */
    @Operation(summary = "List all active ingredients")
    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientDTO>> getAllIngredients() {
        return ResponseEntity.ok(inventoryService.getAllIngredients());
    }

    /**
     * Health check endpoint.
     */
    @Operation(summary = "Health check", description = "Simple liveness probe")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory Service is healthy");
    }
}
