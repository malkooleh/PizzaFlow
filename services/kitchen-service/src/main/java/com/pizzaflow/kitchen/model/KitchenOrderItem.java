package com.pizzaflow.kitchen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderItem implements Serializable {

    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private String customizations;
    private String specialInstructions;
    private Integer prepTimeMinutes;
}
