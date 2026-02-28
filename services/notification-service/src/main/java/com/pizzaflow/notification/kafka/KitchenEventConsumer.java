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

import java.util.Map;
import java.util.UUID;

/**
 * Kafka consumer for kitchen events.
 * Sends customer notifications when kitchen status changes (order preparing /
 * order ready).
 *
 * <p>
 * Topics consumed:
 * <ul>
 * <li>{@code order.preparing} — emitted by kitchen-service when preparation
 * starts</li>
 * <li>{@code order.ready} — emitted by kitchen-service when order is ready for
 * pickup/delivery</li>
 * </ul>
 *
 * <p>
 * Note: kitchen-service's {@code OrderReadyEvent} carries {@code orderId} as
 * {@code Long}
 * (matching order-service primary key) and {@code customerId} as {@code Long}.
 * The notification reference ID is a synthetic UUID derived from orderId for
 * consistency
 * with the rest of the notification model.
 */
@Component
public class KitchenEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KitchenEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public KitchenEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = { "order.preparing", "order.ready" }, groupId = "notification-service")
    public void handleKitchenEvent(String message, Acknowledgment ack) {
        JsonNode event;
        try {
            event = objectMapper.readTree(message);
        } catch (Exception e) {
            log.error("Failed to parse kitchen event JSON, skipping: {}", e.getMessage(), e);
            ack.acknowledge();
            return;
        }
        String eventType = event.path("eventType").asText();
        switch (eventType) {
            case "ORDER_PREPARING" -> handleOrderPreparing(event);
            case "ORDER_READY" -> handleOrderReady(event);
            default -> log.debug("Ignoring kitchen event type: {}", eventType);
        }
        ack.acknowledge();
    }

    private void handleOrderPreparing(JsonNode event) {
        try {
            Long orderId = event.path("orderId").asLong();
            String orderNumber = event.path("orderNumber").asText();
            Long customerId = event.path("customerId").asLong();
            Integer estimatedMinutes = event.path("estimatedPrepTimeMinutes").asInt(0);

            // Use a deterministic UUID based on orderId for the notification reference
            UUID referenceId = UUID.nameUUIDFromBytes(("order:" + orderId).getBytes());
            UUID userId = UUID.nameUUIDFromBytes(("customer:" + customerId).getBytes());

            Map<String, Object> variables = Map.of(
                    "orderNumber", orderNumber,
                    "estimatedMinutes", String.valueOf(estimatedMinutes));

            String body = estimatedMinutes > 0
                    ? "Your order #" + orderNumber + " is being prepared. Estimated time: " + estimatedMinutes
                            + " minutes."
                    : "Your order #" + orderNumber + " is being prepared.";

            sendNotification(userId, null, NotificationChannel.IN_APP,
                    null, "Order Being Prepared", body,
                    variables, "ORDER_PREPARING", referenceId, ReferenceType.ORDER);

            log.info("Sent order.preparing notification for order {}", orderNumber);

        } catch (Exception e) {
            log.error("Failed to handle ORDER_PREPARING notification: {}", e.getMessage(), e);
        }
    }

    private void handleOrderReady(JsonNode event) {
        try {
            Long orderId = event.path("orderId").asLong();
            String orderNumber = event.path("orderNumber").asText();
            Long customerId = event.path("customerId").asLong();
            String orderType = event.path("orderType").asText("DINE_IN");

            UUID referenceId = UUID.nameUUIDFromBytes(("order:" + orderId).getBytes());
            UUID userId = UUID.nameUUIDFromBytes(("customer:" + customerId).getBytes());

            Map<String, Object> variables = Map.of(
                    "orderNumber", orderNumber,
                    "orderType", orderType);

            String title;
            String body;
            if ("DELIVERY".equalsIgnoreCase(orderType)) {
                title = "Order Ready for Pickup";
                body = "Your order #" + orderNumber + " is ready and a courier will collect it shortly.";
            } else {
                title = "Order Ready";
                body = "Your order #" + orderNumber + " is ready! Please collect it.";
            }

            sendNotification(userId, null, NotificationChannel.IN_APP,
                    null, title, body,
                    variables, "ORDER_READY", referenceId, ReferenceType.ORDER);

            log.info("Sent order.ready notification for order {} (type={})", orderNumber, orderType);

        } catch (Exception e) {
            log.error("Failed to handle ORDER_READY notification: {}", e.getMessage(), e);
        }
    }

    private void sendNotification(
            UUID userId, String recipient, NotificationChannel channel,
            String templateName, String subject, String body,
            Map<String, Object> variables, String eventType,
            UUID referenceId, ReferenceType referenceType) {
        SendNotificationRequest request = new SendNotificationRequest(
                userId, channel, recipient, templateName, subject, body,
                variables, eventType, referenceId, referenceType,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(request);
    }
}
