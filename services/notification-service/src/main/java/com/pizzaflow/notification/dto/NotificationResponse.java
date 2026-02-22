package com.pizzaflow.notification.dto;

import com.pizzaflow.notification.model.Notification;
import com.pizzaflow.notification.model.enums.NotificationChannel;
import com.pizzaflow.notification.model.enums.NotificationStatus;
import com.pizzaflow.notification.model.enums.ReferenceType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    UUID userId,
    NotificationChannel channel,
    String subject,
    String body,
    String recipient,
    NotificationStatus status,
    String eventType,
    UUID referenceId,
    ReferenceType referenceType,
    LocalDateTime sentAt,
    LocalDateTime deliveredAt,
    LocalDateTime readAt,
    LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getUserId(),
            notification.getChannel(),
            notification.getSubject(),
            notification.getBody(),
            notification.getRecipient(),
            notification.getStatus(),
            notification.getEventType(),
            notification.getReferenceId(),
            notification.getReferenceType(),
            notification.getSentAt(),
            notification.getDeliveredAt(),
            notification.getReadAt(),
            notification.getCreatedAt()
        );
    }
}
