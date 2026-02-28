package com.pizzaflow.kitchen.exception;

public class InvalidKitchenOrderStateException extends RuntimeException {

    public InvalidKitchenOrderStateException(String message) {
        super(message);
    }
}
