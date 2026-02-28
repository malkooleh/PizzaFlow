package com.pizzaflow.payment.exception;

public class DuplicatePaymentException extends RuntimeException {

    public DuplicatePaymentException(Long orderId) {
        super("Payment already exists for order: " + orderId);
    }
}
