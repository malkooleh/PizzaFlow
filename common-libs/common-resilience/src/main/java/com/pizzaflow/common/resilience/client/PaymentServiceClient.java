package com.pizzaflow.common.resilience.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Feign client for Payment Service.
 */
@FeignClient(
    name = "payment-service",
    fallback = PaymentServiceClient.PaymentServiceFallback.class,
    configuration = com.pizzaflow.common.resilience.config.FeignClientConfig.class
)
public interface PaymentServiceClient {

    @PostMapping("/api/v1/payments/process")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);

    @GetMapping("/api/v1/payments/{paymentId}")
    PaymentResponse getPayment(@PathVariable("paymentId") UUID paymentId);

    @GetMapping("/api/v1/payments/order/{orderId}")
    PaymentResponse getPaymentByOrderId(@PathVariable("orderId") UUID orderId);

    @PostMapping("/api/v1/payments/{paymentId}/refund")
    RefundResponse refundPayment(@PathVariable("paymentId") UUID paymentId, 
                                  @RequestBody RefundRequest request);

    // DTO classes
    record PaymentRequest(
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        String cardNumber,
        String cardExpiry,
        String cardCvv
    ) {}

    enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, CASH, DIGITAL_WALLET
    }

    record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String transactionId,
        String message,
        LocalDateTime processedAt
    ) {}

    enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED, PARTIALLY_REFUNDED
    }

    record RefundRequest(
        BigDecimal amount,
        String reason
    ) {}

    record RefundResponse(
        UUID refundId,
        UUID paymentId,
        BigDecimal refundedAmount,
        RefundStatus status,
        String message,
        LocalDateTime processedAt
    ) {}

    enum RefundStatus {
        PENDING, COMPLETED, FAILED
    }

    // Fallback implementation
    class PaymentServiceFallback implements PaymentServiceClient {
        
        @Override
        public PaymentResponse processPayment(PaymentRequest request) {
            return new PaymentResponse(
                null,
                request.orderId(),
                request.amount(),
                request.currency(),
                PaymentStatus.FAILED,
                null,
                "Payment service unavailable. Please try again later.",
                LocalDateTime.now()
            );
        }

        @Override
        public PaymentResponse getPayment(UUID paymentId) {
            return new PaymentResponse(
                paymentId,
                null,
                BigDecimal.ZERO,
                "USD",
                PaymentStatus.PENDING,
                null,
                "Unable to retrieve payment status",
                null
            );
        }

        @Override
        public PaymentResponse getPaymentByOrderId(UUID orderId) {
            return new PaymentResponse(
                null,
                orderId,
                BigDecimal.ZERO,
                "USD",
                PaymentStatus.PENDING,
                null,
                "Unable to retrieve payment status",
                null
            );
        }

        @Override
        public RefundResponse refundPayment(UUID paymentId, RefundRequest request) {
            return new RefundResponse(
                null,
                paymentId,
                BigDecimal.ZERO,
                RefundStatus.FAILED,
                "Refund service unavailable",
                LocalDateTime.now()
            );
        }
    }
}
