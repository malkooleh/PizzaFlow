package com.pizzaflow.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_levels", indexes = {
        @Index(name = "idx_stock_levels_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_stock_levels_ingredient", columnList = "ingredient_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_stock_ingredient_restaurant", columnNames = { "ingredient_id", "restaurant_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "current_quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal currentQuantity;

    @Column(name = "reserved_quantity", nullable = false, precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    // This is a computed column in the database, so we make it read-only
    @Column(name = "available_quantity", insertable = false, updatable = false)
    private BigDecimal availableQuantity;

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Integer version;

    /**
     * Check if stock is below minimum level.
     */
    public boolean isLowStock() {
        return availableQuantity != null &&
                ingredient != null &&
                availableQuantity.compareTo(ingredient.getMinimumStockLevel()) <= 0;
    }
}
