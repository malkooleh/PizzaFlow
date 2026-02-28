package com.pizzaflow.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaflow.notification.dto.SendNotificationRequest;
import com.pizzaflow.notification.model.enums.NotificationChannel;
import com.pizzaflow.notification.model.enums.NotificationPriority;
import com.pizzaflow.notification.model.enums.ReferenceType;
import com.pizzaflow.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka consumer for payment lifecycle events.
 *
 * <p>
 * Event schema (as produced by payment-service {@code KafkaProducerService}):
 * <ul>
 * <li>{@code PAYMENT_COMPLETED}: transactionId (UUID), orderId (Long),
 * customerId (Long),
 * amount, currency, paymentMethod, gatewayTransactionId, eventType</li>
 * <li>{@code PAYMENT_FAILED}: transactionId (UUID), orderId (Long), customerId
 * (Long),
 * amount, failureReason, errorCode, eventType</li>
 * <li>{@code REFUND_COMPLETED}: refundId (UUID), transactionId (UUID), orderId
 * (Long),
 * customerId (Long), amount, reason, eventType</li>
 * </ul>
 *
 * <p>
 * Because payment events carry {@code customerId} as a {@code Long} (not a
 * UUID), a
 * deterministic UUID is derived via {@link UUID#nameUUIDFromBytes} for use as
 * the
 * notification {@code userId} reference. Email notifications are omitted since
 * the event schema does not carry a user email address.
 */
@Component
public class PaymentEventConsumer {

        private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

        private final NotificationService notificationService;
        private final ObjectMapper objectMapper;

        public PaymentEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
                this.notificationService = notificationService;
                this.objectMapper = objectMapper;
        }

        @KafkaListener(topics = { "payment.completed", "payment.failed",
                        "refund.completed" }, groupId = "notification-service")
        public void handlePaymentEvent(String message, Acknowledgment ack) {
                JsonNode event;
                try {
                        event = objectMapper.readTree(message);
                } catch (Exception e) {
                        log.error("Failed to parse payment event JSON, skipping: {}", e.getMessage(), e);
                        ack.acknowledge();
                        return;
                }
                String eventType = event.path("eventType").asText();
                switch (eventType) {
                        case "PAYMENT_COMPLETED" -> handlePaymentCompleted(event);
                        case "PAYMENT_FAILED" -> handlePaymentFailed(event);
                        case "REFUND_COMPLETED" -> handleRefundCompleted(event);
                        default -> log.debug("Ignoring payment event type: {}", eventType);
                }
                ack.acknowledge();
        }

        // ── handlers ──────────────────────────────────────────────────────────────

        private void handlePaymentCompleted(JsonNode event) {
                UUID transactionId = UUID.fromString(event.path("transactionId").asText());
                long orderIdLong = event.path("orderId").asLong();
                long customerIdLong = event.path("customerId").asLong();
                BigDecimal amount = new BigDecimal(event.path("amount").asText("0"));
                String gatewayRef = event.path("gatewayTransactionId").asText();

                UUID userId = deriveUuid("customer", customerIdLong);
                UUID referenceId = UUID.nameUUIDFromBytes(("transaction:" + transactionId).getBytes());

                Map<String, Object> variables = Map.of(
                                "amount", String.format("$%.2f", amount),
                                "transactionRef", gatewayRef,
                                "orderId", String.valueOf(orderIdLong));

                sendNotification(userId, null, NotificationChannel.IN_APP,
                                null, "Payment Successful",
                                "Your payment of " + variables.get("amount") + " was successful.",
                                variables, "PAYMENT_COMPLETED", referenceId, ReferenceType.PAYMENT,
                                NotificationPriority.NORMAL);

                log.info("Sent payment confirmation notification for order {}", orderIdLong);
        }

        private void handlePaymentFailed(JsonNode event) {
                UUID transactionId = UUID.fromString(event.path("transactionId").asText());
                long orderIdLong = event.path("orderId").asLong();
                long customerIdLong = event.path("customerId").asLong();
                String failureReason = event.path("failureReason").asText("Unknown error");

                UUID userId = deriveUuid("customer", customerIdLong);
                UUID referenceId = UUID.nameUUIDFromBytes(("transaction:" + transactionId).getBytes());

                Map<String, Object> variables = Map.of("reason", failureReason);

                sendNotification(userId, null, NotificationChannel.IN_APP,
                                null, "Payment Failed",
                                "Your payment could not be processed. Reason: " + failureReason,
                                variables, "PAYMENT_FAILED", referenceId, ReferenceType.PAYMENT,
                                NotificationPriority.HIGH);

                log.info("Sent payment failure notification for order {}", orderIdLong);
        }

        private void handleRefundCompleted(JsonNode event) {
                UUID refundId = UUID.fromString(event.path("refundId").asText());
                long orderIdLong = event.path("orderId").asLong();
                long customerIdLong = event.path("customerId").asLong();
                BigDecimal refundAmount = new BigDecimal(event.path("amount").asText("0"));

                UUID userId = deriveUuid("customer", customerIdLong);

                Map<String, Object> variables = Map.of(
                                "refundAmount", String.format("$%.2f", refundAmount));

                sendNotification(userId, null, NotificationChannel.IN_APP,
                                null, "Refund Processed",
                                "Your refund of " + variables.get("refundAmount") + " has been processed.",
                                variables, "REFUND_COMPLETED", refundId, ReferenceType.PAYMENT,
                                NotificationPriority.NORMAL);

                log.info("Sent refund notification for order {}", orderIdLong);
        }

        // ── helpers ───────────────────────────────────────────────────────────────

        /** Derives a stable, reproducible UUID from a numeric domain ID. */
        private static UUID deriveUuid(String prefix, long id) {
                return UUID.nameUUIDFromBytes((prefix + ":" + id).getBytes());
        }

        private void sendNotification(
                        UUID userId, String recipient, NotificationChannel channel,
                        String templateName, String subject, String body,
                        Map<String, Object> variables, String eventType,
                        UUID referenceId, ReferenceType referenceType,
                        NotificationPriority priority) {
                notificationService.sendNotification(new SendNotificationRequest(
                                userId, channel, recipient, templateName, subject, body,
                                variables, eventType, referenceId, referenceType, priority, null));
        }
}
