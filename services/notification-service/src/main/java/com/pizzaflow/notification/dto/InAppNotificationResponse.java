package com.pizzaflow.notification.dto;

import com.pizzaflow.notification.model.InAppNotification;
import com.pizzaflow.notification.model.enums.NotificationPriority;
import com.pizzaflow.notification.model.enums.ReferenceType;

import java.time.LocalDateTime;
import java.util.UUID;

public record InAppNotificationResponse(
    UUID id,
    String title,
    String message,
    String icon,
    String actionUrl,
    String eventType,
    UUID referenceId,
    ReferenceType referenceType,
    boolean isRead,
    NotificationPriority priority,
    LocalDateTime createdAt
) {
    public static InAppNotificationResponse from(InAppNotification notification) {
        return new InAppNotificationResponse(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getIcon(),
            notification.getActionUrl(),
            notification.getEventType(),
            notification.getReferenceId(),
            notification.getReferenceType(),
            notification.isRead(),
            notification.getPriority(),
            notification.getCreatedAt()
        );
    }
}
