package com.pizzaflow.order.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found: " + orderId);
    }

    public OrderNotFoundException(String orderNumber) {
        super("Order not found: " + orderNumber);
    }
}
