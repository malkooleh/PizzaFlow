package com.pizzaflow.order.model.enums;

public enum OrderType {
    DELIVERY,       // Standard home delivery
    PICKUP,         // Customer pickup from restaurant
    DINE_IN,        // Eat at restaurant
    SCHEDULED,      // Pre-scheduled order for future
    HYBRID          // Pre-order for table reservation
}
