package com.pizzaflow.kitchen.dto;

import com.pizzaflow.kitchen.model.enums.KitchenOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private KitchenOrderStatus status;

    private String assignedStation;
}
