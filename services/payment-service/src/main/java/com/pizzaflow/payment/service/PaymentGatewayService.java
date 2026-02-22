package com.pizzaflow.payment.service;

import com.pizzaflow.payment.model.enums.PaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Mock Payment Gateway Service for development and testing.
 * Simulates payment processing with configurable success/failure rates.
 */
@Slf4j
@Service
public class PaymentGatewayService {

    private static final String GATEWAY_NAME = "MockGateway";
    private static final double SUCCESS_RATE = 0.95; // 95% success rate
    private final Random random = new Random();

    /**
     * Process a payment through the mock gateway.
     *
     * @param amount            Payment amount
     * @param currency          Currency code
     * @param paymentMethodType Type of payment method
     * @param cardLastFour      Last 4 digits of card (for simulation)
     * @return Gateway response with transaction details
     */
    public GatewayResponse processPayment(
            BigDecimal amount,
            String currency,
            PaymentMethodType paymentMethodType,
            String cardLastFour) {

        log.info("Processing payment: amount={}, currency={}, method={}, cardLastFour={}",
                amount, currency, paymentMethodType, cardLastFour);

        // Simulate network latency (100-500ms)
        simulateLatency();

        // Generate a mock gateway transaction ID
        String gatewayTransactionId = "TXN_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // Simulate payment processing with configurable success rate
        boolean isSuccess = random.nextDouble() < SUCCESS_RATE;

        // Special test cases based on card last four digits
        if (cardLastFour != null) {
            switch (cardLastFour) {
                case "0000" -> isSuccess = false; // Always fail
                case "1111" -> isSuccess = true; // Always succeed
                case "9999" -> {
                    // Simulate timeout/delayed response
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    isSuccess = true;
                }
            }
        }

        if (isSuccess) {
            log.info("Payment successful: gatewayTransactionId={}", gatewayTransactionId);
            return GatewayResponse.builder()
                    .success(true)
                    .gatewayName(GATEWAY_NAME)
                    .gatewayTransactionId(gatewayTransactionId)
                    .message("Payment processed successfully")
                    .responseCode("00")
                    .build();
        } else {
            String errorCode = getRandomErrorCode();
            String errorMessage = getErrorMessage(errorCode);
            log.warn("Payment failed: errorCode={}, message={}", errorCode, errorMessage);
            return GatewayResponse.builder()
                    .success(false)
                    .gatewayName(GATEWAY_NAME)
                    .gatewayTransactionId(gatewayTransactionId)
                    .message(errorMessage)
                    .responseCode(errorCode)
                    .errorCode(errorCode)
                    .build();
        }
    }

    /**
     * Process a refund through the mock gateway.
     */
    public GatewayResponse processRefund(String originalTransactionId, BigDecimal amount) {
        log.info("Processing refund: originalTransactionId={}, amount={}", originalTransactionId, amount);

        simulateLatency();

        String refundId = "REF_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // Refunds have higher success rate
        boolean isSuccess = random.nextDouble() < 0.98;

        if (isSuccess) {
            log.info("Refund successful: refundId={}", refundId);
            return GatewayResponse.builder()
                    .success(true)
                    .gatewayName(GATEWAY_NAME)
                    .gatewayTransactionId(refundId)
                    .message("Refund processed successfully")
                    .responseCode("00")
                    .build();
        } else {
            log.warn("Refund failed for transaction: {}", originalTransactionId);
            return GatewayResponse.builder()
                    .success(false)
                    .gatewayName(GATEWAY_NAME)
                    .gatewayTransactionId(refundId)
                    .message("Refund processing failed")
                    .responseCode("RF01")
                    .errorCode("RF01")
                    .build();
        }
    }

    private void simulateLatency() {
        try {
            Thread.sleep(100 + random.nextInt(400));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String getRandomErrorCode() {
        String[] errorCodes = { "51", "54", "61", "65", "91", "96" };
        return errorCodes[random.nextInt(errorCodes.length)];
    }

    private String getErrorMessage(String errorCode) {
        return switch (errorCode) {
            case "51" -> "Insufficient funds";
            case "54" -> "Expired card";
            case "61" -> "Transaction amount exceeds limit";
            case "65" -> "Activity count limit exceeded";
            case "91" -> "Issuer or switch inoperative";
            case "96" -> "System malfunction";
            default -> "Transaction declined";
        };
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayResponse {
        private boolean success;
        private String gatewayName;
        private String gatewayTransactionId;
        private String message;
        private String responseCode;
        private String errorCode;
    }
}
