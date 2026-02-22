package com.pizzaflow.delivery.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TrackingInfo(
    UUID deliveryId,
    UUID courierId,
    String courierName,
    BigDecimal courierLatitude,
    BigDecimal courierLongitude,
    BigDecimal deliveryLatitude,
    BigDecimal deliveryLongitude,
    Integer estimatedMinutesRemaining,
    BigDecimal distanceRemainingKm,
    String status,
    LocalDateTime lastUpdate
) {
}
