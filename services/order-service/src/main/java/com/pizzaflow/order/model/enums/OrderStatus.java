package com.pizzaflow.order.model.enums;

public enum OrderStatus {
    PENDING,        // Initial state, payment pending
    CONFIRMED,      // Payment received, order confirmed
    PREPARING,      // Kitchen started preparing
    READY,          // Food ready for pickup/delivery
    OUT_FOR_DELIVERY, // Courier picked up order
    DELIVERED,      // Successfully delivered
    COMPLETED,      // Order completed (dine-in finished)
    CANCELLED,      // Order cancelled
    FAILED          // Order failed (payment/inventory issues)
}
