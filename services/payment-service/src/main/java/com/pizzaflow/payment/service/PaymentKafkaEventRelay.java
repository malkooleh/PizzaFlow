package com.pizzaflow.payment.service;

import com.pizzaflow.payment.event.internal.PaymentCompletedApplicationEvent;
import com.pizzaflow.payment.event.internal.PaymentFailedApplicationEvent;
import com.pizzaflow.payment.event.internal.RefundCompletedApplicationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * Relays payment domain events to Kafka after the DB transaction has committed.
 *
 * <p>
 * Using {@code AFTER_COMMIT} guarantees that Kafka messages are never emitted
 * for transactions that were ultimately rolled back — eliminating the "ghost
 * event"
 * problem where downstream services react to data that never persisted.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaEventRelay {

    private final KafkaProducerService kafkaProducerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedApplicationEvent event) {
        log.debug("Relaying payment.completed to Kafka for order: {}", event.orderId());
        kafkaProducerService.publishPaymentCompleted(
                event.transactionId(),
                event.orderId(),
                event.customerId(),
                event.amount(),
                event.currency(),
                event.paymentMethod(),
                event.gatewayTransactionId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentFailed(PaymentFailedApplicationEvent event) {
        log.debug("Relaying payment.failed to Kafka for order: {}", event.orderId());
        kafkaProducerService.publishPaymentFailed(
                event.transactionId(),
                event.orderId(),
                event.customerId(),
                event.amount(),
                event.failureReason(),
                event.errorCode());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRefundCompleted(RefundCompletedApplicationEvent event) {
        log.debug("Relaying refund.completed to Kafka for order: {}", event.orderId());
        kafkaProducerService.publishRefundCompleted(
                event.refundId(),
                event.transactionId(),
                event.orderId(),
                event.customerId(),
                event.amount(),
                event.reason());
    }
}
