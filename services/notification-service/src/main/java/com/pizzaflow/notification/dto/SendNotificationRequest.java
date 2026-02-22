package com.pizzaflow.notification.dto;

import com.pizzaflow.notification.model.enums.NotificationChannel;
import com.pizzaflow.notification.model.enums.NotificationPriority;
import com.pizzaflow.notification.model.enums.ReferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record SendNotificationRequest(
    @NotNull(message = "User ID is required")
    UUID userId,

    @NotNull(message = "Channel is required")
    NotificationChannel channel,

    @NotBlank(message = "Recipient is required")
    String recipient,  // Email, phone, or device token

    String templateName,  // Use template or provide direct content

    String subject,
    String body,

    Map<String, Object> variables,  // For template substitution

    String eventType,
    UUID referenceId,
    ReferenceType referenceType,

    NotificationPriority priority,
    Map<String, Object> metadata
) {
}
