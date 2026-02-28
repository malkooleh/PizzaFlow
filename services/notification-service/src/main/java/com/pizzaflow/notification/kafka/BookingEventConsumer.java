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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' h:mm a");

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public BookingEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "booking-events", groupId = "notification-service")
    public void handleBookingEvent(String message, Acknowledgment ack) {
        JsonNode event;
        try {
            event = objectMapper.readTree(message);
        } catch (Exception e) {
            log.error("Failed to parse booking event JSON, skipping: {}", e.getMessage(), e);
            ack.acknowledge();
            return;
        }
        String eventType = event.path("eventType").asText();
        switch (eventType) {
            case "booking.created" -> handleBookingCreated(event);
            case "booking.confirmed" -> handleBookingConfirmed(event);
            case "booking.cancelled" -> handleBookingCancelled(event);
            case "booking.reminder" -> handleBookingReminder(event);
            default -> log.debug("Ignoring booking event type: {}", eventType);
        }
        ack.acknowledge();
    }

    private void handleBookingCreated(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID bookingId = UUID.fromString(event.path("bookingId").asText());
        String userEmail = event.path("userEmail").asText();
        String restaurantName = event.path("restaurantName").asText();
        String bookingTime = event.path("bookingTime").asText();
        int partySize = event.path("partySize").asInt();

        String formattedTime = formatDateTime(bookingTime);

        Map<String, Object> variables = Map.of(
                "restaurantName", restaurantName,
                "bookingTime", formattedTime,
                "partySize", partySize);

        // Email confirmation
        SendNotificationRequest emailRequest = new SendNotificationRequest(
                userId, NotificationChannel.EMAIL, userEmail,
                "booking-confirmation", "Booking Confirmation",
                "Your table for " + partySize + " at " + restaurantName + " on " + formattedTime + " has been created.",
                variables, "booking.created", bookingId, ReferenceType.BOOKING,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(emailRequest);

        // In-app notification
        SendNotificationRequest inAppRequest = new SendNotificationRequest(
                userId, NotificationChannel.IN_APP, null,
                null, "Booking Created",
                "Your table reservation at " + restaurantName + " is pending confirmation.",
                variables, "booking.created", bookingId, ReferenceType.BOOKING,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(inAppRequest);

        log.info("Sent booking created notifications for booking {}", bookingId);
    }

    private void handleBookingConfirmed(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID bookingId = UUID.fromString(event.path("bookingId").asText());
        String userEmail = event.path("userEmail").asText();
        String restaurantName = event.path("restaurantName").asText();
        String bookingTime = event.path("bookingTime").asText();
        String tableName = event.path("tableName").asText();

        String formattedTime = formatDateTime(bookingTime);

        Map<String, Object> variables = Map.of(
                "restaurantName", restaurantName,
                "bookingTime", formattedTime,
                "tableName", tableName);

        // Email confirmation
        SendNotificationRequest emailRequest = new SendNotificationRequest(
                userId, NotificationChannel.EMAIL, userEmail,
                "booking-confirmed", "Booking Confirmed",
                "Your reservation at " + restaurantName + " is confirmed for " + formattedTime + ".",
                variables, "booking.confirmed", bookingId, ReferenceType.BOOKING,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(emailRequest);

        // In-app notification
        SendNotificationRequest inAppRequest = new SendNotificationRequest(
                userId, NotificationChannel.IN_APP, null,
                null, "Booking Confirmed!",
                "Your table at " + restaurantName + " is confirmed for " + formattedTime + ".",
                variables, "booking.confirmed", bookingId, ReferenceType.BOOKING,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(inAppRequest);

        log.info("Sent booking confirmed notifications for booking {}", bookingId);
    }

    private void handleBookingCancelled(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID bookingId = UUID.fromString(event.path("bookingId").asText());
        String userEmail = event.path("userEmail").asText();
        String restaurantName = event.path("restaurantName").asText();
        String bookingTime = event.path("bookingTime").asText();
        String reason = event.path("reason").asText("");

        String formattedTime = formatDateTime(bookingTime);

        Map<String, Object> variables = Map.of(
                "restaurantName", restaurantName,
                "bookingTime", formattedTime,
                "reason", reason);

        // Email notification
        SendNotificationRequest emailRequest = new SendNotificationRequest(
                userId, NotificationChannel.EMAIL, userEmail,
                null, "Booking Cancelled",
                "Your reservation at " + restaurantName + " for " + formattedTime + " has been cancelled.",
                variables, "booking.cancelled", bookingId, ReferenceType.BOOKING,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(emailRequest);

        // In-app notification
        SendNotificationRequest inAppRequest = new SendNotificationRequest(
                userId, NotificationChannel.IN_APP, null,
                null, "Booking Cancelled",
                "Your reservation at " + restaurantName + " has been cancelled.",
                variables, "booking.cancelled", bookingId, ReferenceType.BOOKING,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(inAppRequest);

        log.info("Sent booking cancelled notifications for booking {}", bookingId);
    }

    private void handleBookingReminder(JsonNode event) {
        UUID userId = UUID.fromString(event.path("userId").asText());
        UUID bookingId = UUID.fromString(event.path("bookingId").asText());
        String userEmail = event.path("userEmail").asText();
        String restaurantName = event.path("restaurantName").asText();
        String bookingTime = event.path("bookingTime").asText();
        String restaurantAddress = event.path("restaurantAddress").asText();
        int hoursUntil = event.path("hoursUntil").asInt(24);

        String formattedTime = formatDateTime(bookingTime);

        Map<String, Object> variables = Map.of(
                "restaurantName", restaurantName,
                "bookingTime", formattedTime,
                "restaurantAddress", restaurantAddress,
                "hoursUntil", hoursUntil);

        // Email reminder
        SendNotificationRequest emailRequest = new SendNotificationRequest(
                userId, NotificationChannel.EMAIL, userEmail,
                "booking-reminder", "Booking Reminder",
                "Reminder: You have a reservation at " + restaurantName + " " + formattedTime + ".",
                variables, "booking.reminder", bookingId, ReferenceType.BOOKING,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(emailRequest);

        // Push notification for reminder
        SendNotificationRequest pushRequest = new SendNotificationRequest(
                userId, NotificationChannel.PUSH, null,
                null, "Upcoming Reservation",
                "Don't forget! Your reservation at " + restaurantName + " is in " + hoursUntil + " hours.",
                variables, "booking.reminder", bookingId, ReferenceType.BOOKING,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(pushRequest);

        // In-app notification
        SendNotificationRequest inAppRequest = new SendNotificationRequest(
                userId, NotificationChannel.IN_APP, null,
                null, "Reservation Reminder",
                "Your reservation at " + restaurantName + " is coming up " + formattedTime + ".",
                variables, "booking.reminder", bookingId, ReferenceType.BOOKING,
                NotificationPriority.NORMAL, null);
        notificationService.sendNotification(inAppRequest);

        log.info("Sent booking reminder notifications for booking {}", bookingId);
    }

    private String formatDateTime(String isoDateTime) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoDateTime);
            return dateTime.format(DATE_FORMATTER);
        } catch (Exception e) {
            return isoDateTime;
        }
    }
}
