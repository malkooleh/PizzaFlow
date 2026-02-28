package com.pizzaflow.catalog.controller;

import com.pizzaflow.catalog.model.MenuItem;
import com.pizzaflow.catalog.service.CatalogService;
import com.pizzaflow.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Menu and product catalog — browse, search, and manage menu items")
public class CatalogController {

    private final CatalogService catalogService;

    @Operation(summary = "Get full menu for a restaurant")
    @GetMapping("/menu/{restaurantId}")
    public ResponseEntity<ApiResponse<List<MenuItem>>> getMenu(
            @PathVariable String restaurantId) {
        List<MenuItem> menu = catalogService.getMenuByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    @Operation(summary = "Get menu items by category")
    @GetMapping("/menu/{restaurantId}/category/{category}")
    public ResponseEntity<ApiResponse<List<MenuItem>>> getMenuByCategory(
            @PathVariable String restaurantId,
            @PathVariable String category) {
        List<MenuItem> items = catalogService.getMenuByCategory(restaurantId, category);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @Operation(summary = "Get featured menu items")
    @GetMapping("/menu/{restaurantId}/featured")
    public ResponseEntity<ApiResponse<List<MenuItem>>> getFeaturedItems(
            @PathVariable String restaurantId) {
        List<MenuItem> items = catalogService.getFeaturedItems(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @Operation(summary = "Search menu items by keyword")
    @GetMapping("/menu/{restaurantId}/search")
    public ResponseEntity<ApiResponse<List<MenuItem>>> searchMenu(
            @PathVariable String restaurantId,
            @RequestParam String query) {
        List<MenuItem> items = catalogService.searchMenuItems(restaurantId, query);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @Operation(summary = "Get a single menu item by ID")
    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> getMenuItem(@PathVariable String id) {
        return catalogService.getMenuItem(id)
                .map(item -> ResponseEntity.ok(ApiResponse.success(item)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Menu item not found", null)));
    }

    @Operation(summary = "Create a new menu item")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<MenuItem>> createMenuItem(
            @Valid @RequestBody MenuItem menuItem) {
        MenuItem created = catalogService.createMenuItem(menuItem);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Menu item created successfully"));
    }

    @Operation(summary = "Update a menu item")
    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> updateMenuItem(
            @PathVariable String id,
            @Valid @RequestBody MenuItem menuItem) {
        MenuItem updated = catalogService.updateMenuItem(id, menuItem);
        return ResponseEntity.ok(ApiResponse.success(updated, "Menu item updated successfully"));
    }

    @Operation(summary = "Delete a menu item")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable String id) {
        catalogService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Menu item deleted successfully"));
    }
}
