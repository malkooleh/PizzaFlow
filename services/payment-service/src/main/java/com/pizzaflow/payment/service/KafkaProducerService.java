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

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

        private final KafkaTemplate<String, Object> kafkaTemplate;

        private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
        private static final String PAYMENT_FAILED_TOPIC = "payment.failed";
        private static final String REFUND_COMPLETED_TOPIC = "refund.completed";

        public void publishPaymentCompleted(UUID transactionId, Long orderId, Long customerId,
                        BigDecimal amount, String currency, String paymentMethod,
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

                kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, orderId.toString(), event)
                                .whenComplete((result, ex) -> {
                                        if (ex != null) {
                                                log.error("Failed to publish payment.completed to Kafka for order {}: {}",
                                                                orderId, ex.getMessage(), ex);
                                        } else {
                                                log.info("Published payment.completed event for order: {}", orderId);
                                        }
                                });
        }

        public void publishPaymentFailed(UUID transactionId, Long orderId, Long customerId,
                        BigDecimal amount, String failureReason, String errorCode) {
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

                kafkaTemplate.send(PAYMENT_FAILED_TOPIC, orderId.toString(), event)
                                .whenComplete((result, ex) -> {
                                        if (ex != null) {
                                                log.error("Failed to publish payment.failed to Kafka for order {}: {}",
                                                                orderId, ex.getMessage(), ex);
                                        } else {
                                                log.info("Published payment.failed event for order: {}", orderId);
                                        }
                                });
        }

        public void publishRefundCompleted(UUID refundId, UUID transactionId, Long orderId,
                        Long customerId, BigDecimal amount, String reason) {
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

                kafkaTemplate.send(REFUND_COMPLETED_TOPIC, orderId.toString(), event)
                                .whenComplete((result, ex) -> {
                                        if (ex != null) {
                                                log.error("Failed to publish refund.completed to Kafka for order {}: {}",
                                                                orderId, ex.getMessage(), ex);
                                        } else {
                                                log.info("Published refund.completed event for order: {}", orderId);
                                        }
                                });
        }
}
