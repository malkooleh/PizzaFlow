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

@Component
public class DeliveryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public DeliveryEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "delivery-events", groupId = "notification-service")
    public void handleDeliveryEvent(String message, Acknowledgment ack) {
        JsonNode event;
        try {
            event = objectMapper.readTree(message);
        } catch (Exception e) {
            log.error("Failed to parse delivery event JSON, skipping: {}", e.getMessage(), e);
            ack.acknowledge();
            return;
        }
        String eventType = event.path("eventType").asText();
        switch (eventType) {
            case "delivery.assigned" -> handleDeliveryAssigned(event);
            case "delivery.picked_up" -> handleDeliveryPickedUp(event);
            case "delivery.in_transit" -> handleDeliveryInTransit(event);
            case "delivery.arrived" -> handleDeliveryArrived(event);
            case "delivery.completed" -> handleDeliveryCompleted(event);
            default -> log.debug("Ignoring delivery event type: {}", eventType);
        }
        ack.acknowledge();
    }

    private void handleDeliveryAssigned(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID deliveryId = UUID.fromString(event.path("deliveryId").asText());
        String courierName = event.path("courierName").asText();
        String estimatedTime = event.path("estimatedDeliveryTime").asText();

        Map<String, Object> variables = Map.of(
                "courierName", courierName,
                "estimatedTime", estimatedTime);

        SendNotificationRequest request = new SendNotificationRequest(
                userId, NotificationChannel.IN_APP, null,
                null, "Driver Assigned",
                courierName + " is on the way! Estimated delivery: " + estimatedTime,
                variables, "delivery.assigned", deliveryId, ReferenceType.DELIVERY,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(request);

        log.info("Sent delivery assigned notification for delivery {}", deliveryId);
    }

    private void handleDeliveryPickedUp(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID deliveryId = UUID.fromString(event.path("deliveryId").asText());
        String courierName = event.path("courierName").asText();

        Map<String, Object> variables = Map.of("courierName", courierName);

        SendNotificationRequest request = new SendNotificationRequest(
                userId, NotificationChannel.IN_APP, null,
                null, "Order Picked Up",
                "Your order has been picked up by " + courierName + " and is on its way!",
                variables, "delivery.picked_up", deliveryId, ReferenceType.DELIVERY,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(request);

        // Also send push notification for real-time updates
        SendNotificationRequest pushRequest = new SendNotificationRequest(
                userId, NotificationChannel.PUSH, null,
                null, "Order Picked Up",
                "Your order is on its way!",
                variables, "delivery.picked_up", deliveryId, ReferenceType.DELIVERY,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(pushRequest);

        log.info("Sent delivery picked up notifications for delivery {}", deliveryId);
    }

    private void handleDeliveryInTransit(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID deliveryId = UUID.fromString(event.path("deliveryId").asText());
        int minutesAway = event.path("minutesAway").asInt(0);

        Map<String, Object> variables = Map.of("minutesAway", minutesAway);

        if (minutesAway <= 5) {
            SendNotificationRequest request = new SendNotificationRequest(
                    userId, NotificationChannel.IN_APP, null,
                    null, "Almost There!",
                    "Your order is " + minutesAway + " minutes away!",
                    variables, "delivery.in_transit", deliveryId, ReferenceType.DELIVERY,
                    NotificationPriority.HIGH, null);
            notificationService.sendNotification(request);

            // Send push notification for proximity alert
            SendNotificationRequest pushRequest = new SendNotificationRequest(
                    userId, NotificationChannel.PUSH, null,
                    null, "Almost There!",
                    "Your order is " + minutesAway + " minutes away!",
                    variables, "delivery.in_transit", deliveryId, ReferenceType.DELIVERY,
                    NotificationPriority.HIGH, null);
            notificationService.sendNotification(pushRequest);

            log.info("Sent proximity alert for delivery {}", deliveryId);
        }
    }

    private void handleDeliveryArrived(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID deliveryId = UUID.fromString(event.path("deliveryId").asText());
        String userEmail = event.path("userEmail").asText();
        String courierName = event.path("courierName").asText();

        Map<String, Object> variables = Map.of("courierName", courierName);

        // High priority in-app notification
        SendNotificationRequest inAppRequest = new SendNotificationRequest(
                userId, NotificationChannel.IN_APP, null,
                null, "Driver Arrived!",
                courierName + " has arrived with your order!",
                variables, "delivery.arrived", deliveryId, ReferenceType.DELIVERY,
                NotificationPriority.URGENT, null);
        notificationService.sendNotification(inAppRequest);

        // Push notification
        SendNotificationRequest pushRequest = new SendNotificationRequest(
                userId, NotificationChannel.PUSH, null,
                null, "Driver Arrived!",
                "Your driver has arrived with your order!",
                variables, "delivery.arrived", deliveryId, ReferenceType.DELIVERY,
                NotificationPriority.URGENT, null);
        notificationService.sendNotification(pushRequest);

        log.info("Sent delivery arrived notifications for delivery {}", deliveryId);
    }

    private void handleDeliveryCompleted(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID deliveryId = UUID.fromString(event.path("deliveryId").asText());
        UUID orderId = UUID.fromString(event.path("orderId").asText());
        String userEmail = event.path("userEmail").asText();

        Map<String, Object> variables = Map.of(
                "orderId", orderId.toString(),
                "deliveryId", deliveryId.toString());

        // Email notification
        SendNotificationRequest emailRequest = new SendNotificationRequest(
                userId, NotificationChannel.EMAIL, userEmail,
                "delivery-complete", "Order Delivered",
                "Your order has been delivered. Enjoy your meal!",
                variables, "delivery.completed", deliveryId, ReferenceType.DELIVERY,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(emailRequest);

        // In-app notification
        SendNotificationRequest inAppRequest = new SendNotificationRequest(
                userId, NotificationChannel.IN_APP, null,
                null, "Order Delivered",
                "Your order has been delivered. Enjoy your meal! Rate your experience?",
                variables, "delivery.completed", deliveryId, ReferenceType.DELIVERY,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(inAppRequest);

        log.info("Sent delivery completed notifications for delivery {}", deliveryId);
    }
}
