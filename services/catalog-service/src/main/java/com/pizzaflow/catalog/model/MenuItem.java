package com.pizzaflow.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Menu item entity representing pizzas, drinks, sides, etc.
 * 
 * Indexes:
 * - Compound index on restaurantId + available for fast filtering of available
 * items
 * - Compound index on restaurantId + category for category-based queries
 * - Compound index on restaurantId + featured for featured items lookup
 * - Text index on name for full-text search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "menu_items")
@CompoundIndexes({
        @CompoundIndex(name = "restaurant_available_idx", def = "{'restaurantId': 1, 'available': 1}"),
        @CompoundIndex(name = "restaurant_category_idx", def = "{'restaurantId': 1, 'category': 1}"),
        @CompoundIndex(name = "restaurant_featured_idx", def = "{'restaurantId': 1, 'featured': 1}")
})
public class MenuItem {

    @Id
    private String id;

    private String restaurantId;

    @TextIndexed
    private String name;

    private String description;

    private String imageUrl;

    private String category; // PIZZA, DRINK, SIDE, DESSERT

    private BigDecimal basePrice;

    private boolean available;

    private boolean featured;

    private int preparationTimeMinutes;

    private List<String> allergens;

    private List<String> dietaryTags; // VEGETARIAN, VEGAN, GLUTEN_FREE, etc.

    private Map<String, Object> nutritionalInfo; // calories, fat, protein, etc.

    private List<Modifier> modifiers; // Extra cheese, size options, etc.

    private Recipe recipe;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Modifier {
        private String id;
        private String name;
        private String type; // SIZE, TOPPING, EXTRA
        private List<ModifierOption> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifierOption {
        private String id;
        private String name;
        private BigDecimal priceAdjustment;
        private boolean available;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recipe {
        private List<Ingredient> ingredients;
        private String instructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ingredient {
        private String ingredientId;
        private String name;
        private Double quantity;
        private String unit;
    }
}
