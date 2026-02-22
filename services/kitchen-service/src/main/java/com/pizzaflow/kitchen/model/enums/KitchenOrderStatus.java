package com.pizzaflow.kitchen.model.enums;

public enum KitchenOrderStatus {
    RECEIVED, // Order received in kitchen queue
    PREPARING, // Kitchen started cooking
    READY, // Order ready for pickup/delivery
    PICKED_UP, // Customer/courier picked up
    CANCELLED // Order cancelled
}
