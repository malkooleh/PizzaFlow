package com.pizzaflow.inventory.mapper;

import com.pizzaflow.inventory.dto.*;
import com.pizzaflow.inventory.model.Ingredient;
import com.pizzaflow.inventory.model.Reservation;
import com.pizzaflow.inventory.model.StockLevel;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public IngredientDTO toDTO(Ingredient ingredient) {
        return IngredientDTO.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .category(ingredient.getCategory())
                .unitOfMeasure(ingredient.getUnitOfMeasure())
                .minimumStockLevel(ingredient.getMinimumStockLevel())
                .reorderQuantity(ingredient.getReorderQuantity())
                .isActive(ingredient.getIsActive())
                .build();
    }

    public StockLevelDTO toDTO(StockLevel stockLevel) {
        Ingredient ingredient = stockLevel.getIngredient();
        return StockLevelDTO.builder()
                .id(stockLevel.getId())
                .ingredientId(ingredient.getId())
                .ingredientName(ingredient.getName())
                .category(ingredient.getCategory())
                .unitOfMeasure(ingredient.getUnitOfMeasure())
                .restaurantId(stockLevel.getRestaurantId())
                .currentQuantity(stockLevel.getCurrentQuantity())
                .reservedQuantity(stockLevel.getReservedQuantity())
                .availableQuantity(stockLevel.getAvailableQuantity())
                .minimumStockLevel(ingredient.getMinimumStockLevel())
                .lowStock(stockLevel.isLowStock())
                .lastRestockedAt(stockLevel.getLastRestockedAt())
                .build();
    }

    public ReservationItemResponse toReservationItemResponse(Reservation reservation, StockLevel stockLevel) {
        return ReservationItemResponse.builder()
                .reservationId(reservation.getId())
                .ingredientId(reservation.getIngredient().getId())
                .ingredientName(reservation.getIngredient().getName())
                .quantity(reservation.getQuantity())
                .status(reservation.getStatus())
                .availableQuantity(stockLevel != null ? stockLevel.getAvailableQuantity() : null)
                .build();
    }
}
