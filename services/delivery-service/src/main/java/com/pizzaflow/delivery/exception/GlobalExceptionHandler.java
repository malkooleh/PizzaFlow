package com.pizzaflow.delivery.exception;

import com.pizzaflow.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DeliveryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDeliveryNotFound(DeliveryNotFoundException ex) {
        log.warn("Delivery not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(),
                        ApiResponse.ErrorDetails.builder().code("DELIVERY_NOT_FOUND").build()));
    }

    @ExceptionHandler(CourierNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCourierNotFound(CourierNotFoundException ex) {
        log.warn("Courier not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(),
                        ApiResponse.ErrorDetails.builder().code("COURIER_NOT_FOUND").build()));
    }

    @ExceptionHandler(InvalidDeliveryStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidDeliveryState(InvalidDeliveryStateException ex) {
        log.warn("Invalid delivery state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(),
                        ApiResponse.ErrorDetails.builder().code("INVALID_DELIVERY_STATE").build()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error in delivery request");
        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed",
                        ApiResponse.ErrorDetails.builder()
                                .code("VALIDATION_ERROR")
                                .field(fieldErrors)
                                .build()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error in delivery-service: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred",
                        ApiResponse.ErrorDetails.builder().code("INTERNAL_ERROR").build()));
    }
}
