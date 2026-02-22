package com.pizzaflow.delivery.dto;

import com.pizzaflow.delivery.model.Courier;
import com.pizzaflow.delivery.model.enums.CourierStatus;
import com.pizzaflow.delivery.model.enums.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CourierResponse(
    UUID id,
    UUID userId,
    String name,
    String phone,
    String email,
    VehicleType vehicleType,
    String licensePlate,
    CourierStatus status,
    BigDecimal currentLatitude,
    BigDecimal currentLongitude,
    LocalDateTime lastLocationUpdate,
    BigDecimal rating,
    Integer totalDeliveries,
    boolean isActive
) {
    public static CourierResponse from(Courier courier) {
        return new CourierResponse(
            courier.getId(),
            courier.getUserId(),
            courier.getName(),
            courier.getPhone(),
            courier.getEmail(),
            courier.getVehicleType(),
            courier.getLicensePlate(),
            courier.getStatus(),
            courier.getCurrentLatitude(),
            courier.getCurrentLongitude(),
            courier.getLastLocationUpdate(),
            courier.getRating(),
            courier.getTotalDeliveries(),
            courier.isActive()
        );
    }
}
