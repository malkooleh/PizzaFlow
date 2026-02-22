package com.pizzaflow.inventory.dto;

import com.pizzaflow.inventory.model.enums.ReservationStatus;
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
public class ReservationItemResponse {
    private UUID reservationId;
    private UUID ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private ReservationStatus status;
    private BigDecimal availableQuantity;
}
