package com.pizzaflow.payment.controller;

import com.pizzaflow.common.dto.ApiResponse;
import com.pizzaflow.payment.dto.*;
import com.pizzaflow.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Process a new payment.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("REST: Processing payment for order: {}", request.getOrderId());
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment processed successfully"));
    }

    /**
     * Get payment by transaction ID.
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable UUID transactionId) {
        log.info("REST: Getting payment: {}", transactionId);
        PaymentResponse response = paymentService.getPayment(transactionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get payment by order ID.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @PathVariable Long orderId) {
        log.info("REST: Getting payment for order: {}", orderId);
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all payments for a customer.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getCustomerPayments(
            @PathVariable Long customerId) {
        log.info("REST: Getting payments for customer: {}", customerId);
        List<PaymentResponse> payments = paymentService.getCustomerPayments(customerId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /**
     * Process a refund.
     */
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @Valid @RequestBody RefundRequest request) {
        log.info("REST: Processing refund for transaction: {}", request.getTransactionId());
        RefundResponse response = paymentService.processRefund(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Refund processed successfully"));
    }

    /**
     * Get customer's saved payment methods.
     */
    @GetMapping("/methods/{customerId}")
    public ResponseEntity<ApiResponse<List<PaymentMethodDTO>>> getPaymentMethods(
            @PathVariable Long customerId) {
        log.info("REST: Getting payment methods for customer: {}", customerId);
        List<PaymentMethodDTO> methods = paymentService.getCustomerPaymentMethods(customerId);
        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    /**
     * Delete (deactivate) a payment method.
     */
    @DeleteMapping("/methods/{paymentMethodId}")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(
            @PathVariable UUID paymentMethodId) {
        log.info("REST: Deleting payment method: {}", paymentMethodId);
        paymentService.deletePaymentMethod(paymentMethodId);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment method deleted"));
    }
}
