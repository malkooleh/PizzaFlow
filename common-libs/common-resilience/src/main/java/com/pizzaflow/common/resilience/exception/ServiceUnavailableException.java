package com.pizzaflow.common.resilience.exception;

/**
 * Exception thrown when a downstream service is unavailable.
 */
public class ServiceUnavailableException extends RuntimeException {
    
    private final int statusCode;

    public ServiceUnavailableException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ServiceUnavailableException(String message) {
        this(message, 503);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
