package com.pizzaflow.kitchen.exception;

public class KitchenOrderNotFoundException extends RuntimeException {

    public KitchenOrderNotFoundException(Long orderId) {
        super("Kitchen order not found: " + orderId);
    }

    public KitchenOrderNotFoundException(String orderNumber) {
        super("Kitchen order not found: " + orderNumber);
    }
}
