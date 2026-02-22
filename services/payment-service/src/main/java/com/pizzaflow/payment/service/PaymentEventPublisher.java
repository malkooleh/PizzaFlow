package com.pizzaflow.payment.service;

import com.pizzaflow.payment.event.PaymentCompletedEvent;
import com.pizzaflow.payment.event.PaymentFailedEvent;
import com.pizzaflow.payment.event.RefundCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for publishing payment-related events to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    private static final String REFUND_COMPLETED_TOPIC = "refund.completed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompleted(
            UUID transactionId,
            Long orderId,
            Long customerId,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String gatewayTransactionId) {

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("PAYMENT_COMPLETED")
                .timestamp(LocalDateTime.now())
                .source("payment-service")
                .version(1)
                .transactionId(transactionId)
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .currency(currency)
                .paymentMethod(paymentMethod)
                .gatewayTransactionId(gatewayTransactionId)
                .build();

        kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, orderId.toString(), event);
        log.info("Published PaymentCompletedEvent: orderId={}, transactionId={}", orderId, transactionId);
    }

    public void publishPaymentFailed(
            UUID transactionId,
            Long orderId,
            Long customerId,
            BigDecimal amount,
            String failureReason,
            String errorCode) {

        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("PAYMENT_FAILED")
                .timestamp(LocalDateTime.now())
                .source("payment-service")
                .version(1)
                .transactionId(transactionId)
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .failureReason(failureReason)
                .errorCode(errorCode)
                .build();

        kafkaTemplate.send(PAYMENT_FAILED_TOPIC, orderId.toString(), event);
        log.info("Published PaymentFailedEvent: orderId={}, reason={}", orderId, failureReason);
    }

    public void publishRefundCompleted(
            UUID refundId,
            UUID transactionId,
            Long orderId,
            Long customerId,
            BigDecimal amount,
            String reason) {

        RefundCompletedEvent event = RefundCompletedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("REFUND_COMPLETED")
                .timestamp(LocalDateTime.now())
                .source("payment-service")
                .version(1)
                .refundId(refundId)
                .transactionId(transactionId)
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .reason(reason)
                .build();

        kafkaTemplate.send(REFUND_COMPLETED_TOPIC, orderId.toString(), event);
        log.info("Published RefundCompletedEvent: orderId={}, refundId={}", orderId, refundId);
    }
}
