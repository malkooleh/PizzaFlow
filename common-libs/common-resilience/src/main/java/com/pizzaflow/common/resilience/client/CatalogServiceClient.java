package com.pizzaflow.common.resilience.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Feign client for Catalog Service.
 */
@FeignClient(
    name = "catalog-service",
    fallback = CatalogServiceClient.CatalogServiceFallback.class,
    configuration = com.pizzaflow.common.resilience.config.FeignClientConfig.class
)
public interface CatalogServiceClient {

    @GetMapping("/api/v1/menu-items/{menuItemId}")
    MenuItemResponse getMenuItem(@PathVariable("menuItemId") String menuItemId);

    @GetMapping("/api/v1/menu-items")
    List<MenuItemResponse> getMenuItems(@RequestParam("restaurantId") UUID restaurantId);

    @PostMapping("/api/v1/menu-items/validate")
    ValidationResponse validateMenuItems(@RequestBody List<String> menuItemIds);

    @GetMapping("/api/v1/menu-items/{menuItemId}/price")
    BigDecimal getPrice(@PathVariable("menuItemId") String menuItemId);

    // DTO classes
    record MenuItemResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean available,
        List<String> ingredientIds
    ) {}

    record ValidationResponse(
        boolean valid,
        List<String> invalidItems,
        Map<String, String> errors
    ) {}

    // Fallback implementation
    class CatalogServiceFallback implements CatalogServiceClient {
        
        @Override
        public MenuItemResponse getMenuItem(String menuItemId) {
            return new MenuItemResponse(
                menuItemId, 
                "Unavailable", 
                "Service temporarily unavailable", 
                BigDecimal.ZERO, 
                "UNKNOWN",
                false,
                List.of()
            );
        }

        @Override
        public List<MenuItemResponse> getMenuItems(UUID restaurantId) {
            return List.of();
        }

        @Override
        public ValidationResponse validateMenuItems(List<String> menuItemIds) {
            return new ValidationResponse(false, menuItemIds, 
                Map.of("error", "Catalog service unavailable"));
        }

        @Override
        public BigDecimal getPrice(String menuItemId) {
            return BigDecimal.ZERO;
        }
    }
}
