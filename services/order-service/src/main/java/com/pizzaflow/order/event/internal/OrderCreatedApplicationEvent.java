package com.pizzaflow.order.event.internal;

import java.math.BigDecimal;

/**
 * Internal Spring application event published within the DB transaction.
 * The Kafka relay listener fires this to Kafka only after the transaction
 * commits (AFTER_COMMIT phase).
 */
public record OrderCreatedApplicationEvent(
        Long orderId,
        String orderNumber,
        Long customerId,
        Long restaurantId,
        BigDecimal totalAmount,
        String orderType) {
}
