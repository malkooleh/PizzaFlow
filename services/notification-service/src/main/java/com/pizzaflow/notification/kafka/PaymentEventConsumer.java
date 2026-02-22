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

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void handlePaymentEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.path("eventType").asText();

            switch (eventType) {
                case "payment.completed" -> handlePaymentCompleted(event);
                case "payment.failed" -> handlePaymentFailed(event);
                case "payment.refunded" -> handlePaymentRefunded(event);
                default -> log.debug("Ignoring payment event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", e.getMessage());
        }
    }

    private void handlePaymentCompleted(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID paymentId = UUID.fromString(event.path("paymentId").asText());
        UUID orderId = UUID.fromString(event.path("orderId").asText());
        String userEmail = event.path("userEmail").asText();
        BigDecimal amount = new BigDecimal(event.path("amount").asText("0"));
        String transactionRef = event.path("transactionReference").asText();

        Map<String, Object> variables = Map.of(
            "amount", String.format("$%.2f", amount),
            "transactionRef", transactionRef,
            "orderId", orderId.toString()
        );

        // Send email receipt
        SendNotificationRequest emailRequest = new SendNotificationRequest(
            userId, NotificationChannel.EMAIL, userEmail,
            "payment-receipt", "Payment Confirmation",
            "Your payment of " + variables.get("amount") + " has been processed successfully.",
            variables, "payment.completed", paymentId, ReferenceType.PAYMENT,
            NotificationPriority.NORMAL, null
        );
        notificationService.sendNotification(emailRequest);

        // Send in-app notification
        SendNotificationRequest inAppRequest = new SendNotificationRequest(
            userId, NotificationChannel.IN_APP, null,
            null, "Payment Successful",
            "Your payment of " + variables.get("amount") + " was successful.",
            variables, "payment.completed", paymentId, ReferenceType.PAYMENT,
            NotificationPriority.NORMAL, null
        );
        notificationService.sendNotification(inAppRequest);

        log.info("Sent payment confirmation notifications for payment {}", paymentId);
    }

    private void handlePaymentFailed(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID paymentId = UUID.fromString(event.path("paymentId").asText());
        String userEmail = event.path("userEmail").asText();
        String failureReason = event.path("failureReason").asText("Unknown error");

        Map<String, Object> variables = Map.of(
            "reason", failureReason
        );

        // Send email notification
        SendNotificationRequest emailRequest = new SendNotificationRequest(
            userId, NotificationChannel.EMAIL, userEmail,
            null, "Payment Failed",
            "Your payment could not be processed. Reason: " + failureReason,
            variables, "payment.failed", paymentId, ReferenceType.PAYMENT,
            NotificationPriority.HIGH, null
        );
        notificationService.sendNotification(emailRequest);

        // Send in-app notification
        SendNotificationRequest inAppRequest = new SendNotificationRequest(
            userId, NotificationChannel.IN_APP, null,
            null, "Payment Failed",
            "Your payment could not be processed. Please try again.",
            variables, "payment.failed", paymentId, ReferenceType.PAYMENT,
            NotificationPriority.HIGH, null
        );
        notificationService.sendNotification(inAppRequest);

        log.info("Sent payment failure notifications for payment {}", paymentId);
    }

    private void handlePaymentRefunded(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID paymentId = UUID.fromString(event.path("paymentId").asText());
        String userEmail = event.path("userEmail").asText();
        BigDecimal refundAmount = new BigDecimal(event.path("refundAmount").asText("0"));

        Map<String, Object> variables = Map.of(
            "refundAmount", String.format("$%.2f", refundAmount)
        );

        // Send email notification
        SendNotificationRequest emailRequest = new SendNotificationRequest(
            userId, NotificationChannel.EMAIL, userEmail,
            null, "Refund Processed",
            "Your refund of " + variables.get("refundAmount") + " has been processed.",
            variables, "payment.refunded", paymentId, ReferenceType.PAYMENT,
            NotificationPriority.NORMAL, null
        );
        notificationService.sendNotification(emailRequest);

        // Send in-app notification
        SendNotificationRequest inAppRequest = new SendNotificationRequest(
            userId, NotificationChannel.IN_APP, null,
            null, "Refund Processed",
            "Your refund of " + variables.get("refundAmount") + " has been processed.",
            variables, "payment.refunded", paymentId, ReferenceType.PAYMENT,
            NotificationPriority.NORMAL, null
        );
        notificationService.sendNotification(inAppRequest);

        log.info("Sent refund notifications for payment {}", paymentId);
    }
}
