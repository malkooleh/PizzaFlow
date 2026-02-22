package com.pizzaflow.delivery.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LocationUpdateRequest(
    @NotNull(message = "Latitude is required")
    BigDecimal latitude,

    @NotNull(message = "Longitude is required")
    BigDecimal longitude,

    BigDecimal speedKmh,
    
    BigDecimal heading,
    
    BigDecimal accuracyMeters
) {
}
