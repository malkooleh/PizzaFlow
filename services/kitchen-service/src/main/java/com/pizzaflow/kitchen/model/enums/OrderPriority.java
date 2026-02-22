package com.pizzaflow.kitchen.model.enums;

public enum OrderPriority {
    LOW, // Scheduled orders with time buffer
    NORMAL, // Standard orders
    HIGH, // ASAP orders
    URGENT // Delayed orders needing immediate attention
}
