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
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void handleOrderEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.path("eventType").asText();

            switch (eventType) {
                case "order.created" -> handleOrderCreated(event);
                case "order.confirmed" -> handleOrderConfirmed(event);
                case "order.cancelled" -> handleOrderCancelled(event);
                case "order.updated" -> handleOrderUpdated(event);
                default -> log.debug("Ignoring order event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process order event: {}", e.getMessage());
        }
    }

    private void handleOrderCreated(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID orderId = UUID.fromString(event.path("orderId").asText());
        String orderNumber = event.path("orderNumber").asText();
        String userEmail = event.path("userEmail").asText();

        Map<String, Object> variables = new HashMap<>();
        variables.put("orderNumber", orderNumber);
        variables.put("orderId", orderId.toString());

        // Send email notification
        sendNotification(userId, userEmail, NotificationChannel.EMAIL, 
            "order-confirmation", "Order Confirmation", 
            "Your order #" + orderNumber + " has been received.",
            variables, "order.created", orderId, ReferenceType.ORDER);

        // Send in-app notification
        sendNotification(userId, null, NotificationChannel.IN_APP,
            null, "Order Received",
            "Your order #" + orderNumber + " has been received and is being processed.",
            variables, "order.created", orderId, ReferenceType.ORDER);

        log.info("Sent order confirmation notifications for order {}", orderNumber);
    }

    private void handleOrderConfirmed(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID orderId = UUID.fromString(event.path("orderId").asText());
        String orderNumber = event.path("orderNumber").asText();

        Map<String, Object> variables = Map.of(
            "orderNumber", orderNumber,
            "orderId", orderId.toString()
        );

        sendNotification(userId, null, NotificationChannel.IN_APP,
            null, "Order Confirmed",
            "Your order #" + orderNumber + " has been confirmed and is being prepared.",
            variables, "order.confirmed", orderId, ReferenceType.ORDER);

        log.info("Sent order confirmed notification for order {}", orderNumber);
    }

    private void handleOrderCancelled(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID orderId = UUID.fromString(event.path("orderId").asText());
        String orderNumber = event.path("orderNumber").asText();
        String userEmail = event.path("userEmail").asText();
        String reason = event.path("reason").asText("No reason provided");

        Map<String, Object> variables = Map.of(
            "orderNumber", orderNumber,
            "orderId", orderId.toString(),
            "reason", reason
        );

        // Send email notification
        sendNotification(userId, userEmail, NotificationChannel.EMAIL,
            null, "Order Cancelled",
            "Your order #" + orderNumber + " has been cancelled. Reason: " + reason,
            variables, "order.cancelled", orderId, ReferenceType.ORDER);

        // Send in-app notification
        sendNotification(userId, null, NotificationChannel.IN_APP,
            null, "Order Cancelled",
            "Your order #" + orderNumber + " has been cancelled.",
            variables, "order.cancelled", orderId, ReferenceType.ORDER);

        log.info("Sent order cancellation notification for order {}", orderNumber);
    }

    private void handleOrderUpdated(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID orderId = UUID.fromString(event.path("orderId").asText());
        String orderNumber = event.path("orderNumber").asText();
        String newStatus = event.path("status").asText();

        Map<String, Object> variables = Map.of(
            "orderNumber", orderNumber,
            "status", newStatus
        );

        sendNotification(userId, null, NotificationChannel.IN_APP,
            null, "Order Update",
            "Your order #" + orderNumber + " status: " + newStatus,
            variables, "order.updated", orderId, ReferenceType.ORDER);
    }

    private void sendNotification(
        UUID userId, String recipient, NotificationChannel channel,
        String templateName, String subject, String body,
        Map<String, Object> variables, String eventType,
        UUID referenceId, ReferenceType referenceType
    ) {
        SendNotificationRequest request = new SendNotificationRequest(
            userId, channel, recipient, templateName, subject, body,
            variables, eventType, referenceId, referenceType,
            NotificationPriority.NORMAL, null
        );
        notificationService.sendNotification(request);
    }
}
