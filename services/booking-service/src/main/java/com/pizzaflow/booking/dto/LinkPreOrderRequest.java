package com.pizzaflow.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LinkPreOrderRequest(
    @NotNull(message = "Pre-order ID is required")
    UUID preOrderId
) {
}
