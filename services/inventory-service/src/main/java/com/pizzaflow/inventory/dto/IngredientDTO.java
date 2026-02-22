package com.pizzaflow.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDTO {
    private UUID id;
    private String name;
    private String category;
    private String unitOfMeasure;
    private BigDecimal minimumStockLevel;
    private BigDecimal reorderQuantity;
    private Boolean isActive;
}
