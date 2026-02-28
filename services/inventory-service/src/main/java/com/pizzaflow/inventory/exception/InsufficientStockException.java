package com.pizzaflow.inventory.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String itemId, int requested, int available) {
        super(String.format(
                "Insufficient stock for item %s: requested %d but only %d available",
                itemId, requested, available));
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}
