package com.pizzaflow.gateway.controller;

import com.pizzaflow.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fallback controller for circuit breaker patterns.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/catalog")
    public ResponseEntity<ApiResponse<Void>> catalogFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Catalog service is temporarily unavailable. Please try again later.", null));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Void>> ordersFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Order service is temporarily unavailable. Please try again later.", null));
    }

    @GetMapping("/kitchen")
    public ResponseEntity<ApiResponse<Void>> kitchenFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Kitchen service is temporarily unavailable. Please try again later.", null));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<Void>> paymentsFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Payment service is temporarily unavailable. Please try again later.", null));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<Void>> inventoryFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Inventory service is temporarily unavailable. Please try again later.", null));
    }

    @GetMapping("/booking")
    public ResponseEntity<ApiResponse<Void>> bookingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Booking service is temporarily unavailable. Please try again later.", null));
    }

    @GetMapping("/delivery")
    public ResponseEntity<ApiResponse<Void>> deliveryFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Delivery service is temporarily unavailable. Please try again later.", null));
    }

    @GetMapping("/notification")
    public ResponseEntity<ApiResponse<Void>> notificationFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Notification service is temporarily unavailable. Please try again later.",
                        null));
    }
}
