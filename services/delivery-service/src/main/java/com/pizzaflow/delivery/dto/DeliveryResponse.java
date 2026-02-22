package com.pizzaflow.delivery.dto;

import com.pizzaflow.delivery.model.Delivery;
import com.pizzaflow.delivery.model.enums.DeliveryPriority;
import com.pizzaflow.delivery.model.enums.DeliveryStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryResponse(
    UUID id,
    UUID orderId,
    DeliveryStatus status,
    
    // Courier info
    UUID courierId,
    String courierName,
    String courierPhone,
    
    // Addresses
    String pickupAddress,
    BigDecimal pickupLatitude,
    BigDecimal pickupLongitude,
    String deliveryAddress,
    BigDecimal deliveryLatitude,
    BigDecimal deliveryLongitude,
    
    // Customer info
    UUID customerId,
    String customerName,
    
    // Timing
    LocalDateTime estimatedPickupTime,
    LocalDateTime actualPickupTime,
    LocalDateTime estimatedDeliveryTime,
    LocalDateTime actualDeliveryTime,
    
    // Details
    BigDecimal distanceKm,
    Integer estimatedDurationMinutes,
    BigDecimal deliveryFee,
    DeliveryPriority priority,
    
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static DeliveryResponse from(Delivery delivery) {
        return new DeliveryResponse(
            delivery.getId(),
            delivery.getOrderId(),
            delivery.getStatus(),
            delivery.getCourier() != null ? delivery.getCourier().getId() : null,
            delivery.getCourier() != null ? delivery.getCourier().getName() : null,
            delivery.getCourier() != null ? delivery.getCourier().getPhone() : null,
            delivery.getPickupAddress(),
            delivery.getPickupLatitude(),
            delivery.getPickupLongitude(),
            delivery.getDeliveryAddress(),
            delivery.getDeliveryLatitude(),
            delivery.getDeliveryLongitude(),
            delivery.getCustomerId(),
            delivery.getCustomerName(),
            delivery.getEstimatedPickupTime(),
            delivery.getActualPickupTime(),
            delivery.getEstimatedDeliveryTime(),
            delivery.getActualDeliveryTime(),
            delivery.getDistanceKm(),
            delivery.getEstimatedDurationMinutes(),
            delivery.getDeliveryFee(),
            delivery.getPriority(),
            delivery.getCreatedAt(),
            delivery.getUpdatedAt()
        );
    }
}
