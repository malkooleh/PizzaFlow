package com.pizzaflow.delivery.event;

import com.pizzaflow.delivery.model.enums.DeliveryStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryEvent(
    String eventType,
    UUID deliveryId,
    UUID orderId,
    UUID courierId,
    String courierName,
    UUID customerId,
    DeliveryStatus status,
    String deliveryAddress,
    BigDecimal latitude,
    BigDecimal longitude,
    LocalDateTime estimatedDeliveryTime,
    String failureReason,
    LocalDateTime eventTimestamp
) {
    public static final String DELIVERY_CREATED = "delivery.created";
    public static final String COURIER_ASSIGNED = "delivery.courier_assigned";
    public static final String ORDER_PICKED_UP = "delivery.picked_up";
    public static final String DELIVERY_IN_TRANSIT = "delivery.in_transit";
    public static final String COURIER_ARRIVED = "delivery.arrived";
    public static final String DELIVERY_COMPLETED = "delivery.completed";
    public static final String DELIVERY_FAILED = "delivery.failed";
    public static final String LOCATION_UPDATED = "delivery.location_updated";
}
