package com.pizzaflow.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevelDTO {
    private UUID id;
    private UUID ingredientId;
    private String ingredientName;
    private String category;
    private String unitOfMeasure;
    private UUID restaurantId;
    private BigDecimal currentQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal minimumStockLevel;
    private boolean lowStock;
    private LocalDateTime lastRestockedAt;
}
