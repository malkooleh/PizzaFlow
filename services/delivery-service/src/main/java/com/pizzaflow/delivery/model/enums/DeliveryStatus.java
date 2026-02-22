package com.pizzaflow.delivery.model.enums;

public enum DeliveryStatus {
    PENDING,       // Waiting for courier assignment
    ASSIGNED,      // Courier assigned, heading to restaurant
    PICKED_UP,     // Order picked up from restaurant
    IN_TRANSIT,    // On the way to customer
    ARRIVED,       // Courier at delivery location
    DELIVERED,     // Successfully delivered
    FAILED,        // Delivery failed (customer not available, etc.)
    CANCELLED      // Delivery cancelled
}
