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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka consumer for order lifecycle events.
 *
 * <p>
 * Event schema (as produced by order-service {@code OrderKafkaEventRelay}):
 * <ul>
 * <li>{@code ORDER_CREATED}: orderId (Long), customerId (Long), orderNumber,
 * totalAmount,
 * orderType, restaurantId, eventType, eventId, source, version, timestamp</li>
 * </ul>
 *
 * <p>
 * Because order events carry {@code orderId} and {@code customerId} as
 * {@code Long}
 * values, deterministic UUIDs are derived via {@link UUID#nameUUIDFromBytes}
 * for use as
 * notification reference and user IDs. Email notifications are omitted since
 * the event
 * schema does not carry a user email address.
 */
@Component
public class OrderEventConsumer {

        private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

        private final NotificationService notificationService;
        private final ObjectMapper objectMapper;

        public OrderEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
                this.notificationService = notificationService;
                this.objectMapper = objectMapper;
        }

        @KafkaListener(topics = { "order.created", "order.confirmed", "order.cancelled",
                        "order.updated" }, groupId = "notification-service")
        public void handleOrderEvent(String message, Acknowledgment ack) {
                JsonNode event;
                try {
                        event = objectMapper.readTree(message);
                } catch (Exception e) {
                        log.error("Failed to parse order event JSON, skipping: {}", e.getMessage(), e);
                        ack.acknowledge();
                        return;
                }
                String eventType = event.path("eventType").asText();
                switch (eventType) {
                        case "ORDER_CREATED" -> handleOrderCreated(event);
                        case "ORDER_CONFIRMED" -> handleOrderConfirmed(event);
                        case "ORDER_CANCELLED" -> handleOrderCancelled(event);
                        case "ORDER_UPDATED" -> handleOrderUpdated(event);
                        default -> log.debug("Ignoring order event type: {}", eventType);
                }
                ack.acknowledge();
        }

        // ── handlers ──────────────────────────────────────────────────────────────

        private void handleOrderCreated(JsonNode event) {
                long orderIdLong = event.path("orderId").asLong();
                long customerIdLong = event.path("customerId").asLong();
                String orderNumber = event.path("orderNumber").asText();

                UUID userId = deriveUuid("customer", customerIdLong);
                UUID referenceId = deriveUuid("order", orderIdLong);

                Map<String, Object> variables = new HashMap<>();
                variables.put("orderNumber", orderNumber);
                variables.put("orderId", String.valueOf(orderIdLong));

                sendNotification(userId, null, NotificationChannel.IN_APP,
                                null, "Order Received",
                                "Your order #" + orderNumber + " has been received and is being processed.",
                                variables, "ORDER_CREATED", referenceId, ReferenceType.ORDER,
                                NotificationPriority.NORMAL);

                log.info("Sent order confirmation notification for order {}", orderNumber);
        }

        private void handleOrderConfirmed(JsonNode event) {
                long orderIdLong = event.path("orderId").asLong();
                long customerIdLong = event.path("customerId").asLong();
                String orderNumber = event.path("orderNumber").asText();

                UUID userId = deriveUuid("customer", customerIdLong);
                UUID referenceId = deriveUuid("order", orderIdLong);

                Map<String, Object> variables = Map.of(
                                "orderNumber", orderNumber,
                                "orderId", String.valueOf(orderIdLong));

                sendNotification(userId, null, NotificationChannel.IN_APP,
                                null, "Order Confirmed",
                                "Your order #" + orderNumber + " has been confirmed and is being prepared.",
                                variables, "ORDER_CONFIRMED", referenceId, ReferenceType.ORDER,
                                NotificationPriority.NORMAL);

                log.info("Sent order confirmed notification for order {}", orderNumber);
        }

        private void handleOrderCancelled(JsonNode event) {
                long orderIdLong = event.path("orderId").asLong();
                long customerIdLong = event.path("customerId").asLong();
                String orderNumber = event.path("orderNumber").asText();
                String reason = event.path("reason").asText("No reason provided");

                UUID userId = deriveUuid("customer", customerIdLong);
                UUID referenceId = deriveUuid("order", orderIdLong);

                Map<String, Object> variables = Map.of(
                                "orderNumber", orderNumber,
                                "orderId", String.valueOf(orderIdLong),
                                "reason", reason);

                sendNotification(userId, null, NotificationChannel.IN_APP,
                                null, "Order Cancelled",
                                "Your order #" + orderNumber + " has been cancelled. Reason: " + reason,
                                variables, "ORDER_CANCELLED", referenceId, ReferenceType.ORDER,
                                NotificationPriority.HIGH);

                log.info("Sent order cancellation notification for order {}", orderNumber);
        }

        private void handleOrderUpdated(JsonNode event) {
                long orderIdLong = event.path("orderId").asLong();
                long customerIdLong = event.path("customerId").asLong();
                String orderNumber = event.path("orderNumber").asText();
                String newStatus = event.path("status").asText();

                UUID userId = deriveUuid("customer", customerIdLong);
                UUID referenceId = deriveUuid("order", orderIdLong);

                Map<String, Object> variables = Map.of(
                                "orderNumber", orderNumber,
                                "status", newStatus);

                sendNotification(userId, null, NotificationChannel.IN_APP,
                                null, "Order Update",
                                "Your order #" + orderNumber + " status: " + newStatus,
                                variables, "ORDER_UPDATED", referenceId, ReferenceType.ORDER,
                                NotificationPriority.NORMAL);
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
