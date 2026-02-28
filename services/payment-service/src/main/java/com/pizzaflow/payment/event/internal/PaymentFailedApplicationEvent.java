package com.pizzaflow.payment.event.internal;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Internal Spring application event published within the DB transaction.
 * The Kafka relay listener fires this to Kafka only after the transaction
 * commits (AFTER_COMMIT phase).
 */
public record PaymentFailedApplicationEvent(
        UUID transactionId,
        Long orderId,
        Long customerId,
        BigDecimal amount,
        String failureReason,
        String errorCode) {
}
