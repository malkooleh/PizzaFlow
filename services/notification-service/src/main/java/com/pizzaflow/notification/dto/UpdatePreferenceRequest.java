package com.pizzaflow.notification.dto;

import java.time.LocalTime;

public record UpdatePreferenceRequest(
    Boolean emailEnabled,
    Boolean smsEnabled,
    Boolean pushEnabled,
    Boolean inAppEnabled,
    Boolean orderUpdates,
    Boolean paymentNotifications,
    Boolean deliveryTracking,
    Boolean bookingReminders,
    Boolean promotionalMessages,
    LocalTime quietHoursStart,
    LocalTime quietHoursEnd
) {
}
