package com.pizzaflow.delivery.dto;

import com.pizzaflow.delivery.model.enums.DeliveryPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateDeliveryRequest(
    @NotNull(message = "Order ID is required")
    UUID orderId,

    @NotNull(message = "Customer ID is required")
    UUID customerId,

    @NotBlank(message = "Customer name is required")
    String customerName,

    @NotBlank(message = "Customer phone is required")
    String customerPhone,

    @NotBlank(message = "Pickup address is required")
    String pickupAddress,

    @NotNull(message = "Pickup latitude is required")
    BigDecimal pickupLatitude,

    @NotNull(message = "Pickup longitude is required")
    BigDecimal pickupLongitude,

    String pickupInstructions,

    @NotBlank(message = "Delivery address is required")
    String deliveryAddress,

    @NotNull(message = "Delivery latitude is required")
    BigDecimal deliveryLatitude,

    @NotNull(message = "Delivery longitude is required")
    BigDecimal deliveryLongitude,

    String deliveryInstructions,

    BigDecimal deliveryFee,

    DeliveryPriority priority
) {
}
