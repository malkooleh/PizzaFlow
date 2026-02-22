package com.pizzaflow.notification.dto;

import com.pizzaflow.notification.model.NotificationPreference;

import java.time.LocalTime;
import java.util.UUID;

public record PreferenceResponse(
    UUID userId,
    boolean emailEnabled,
    boolean smsEnabled,
    boolean pushEnabled,
    boolean inAppEnabled,
    boolean orderUpdates,
    boolean paymentNotifications,
    boolean deliveryTracking,
    boolean bookingReminders,
    boolean promotionalMessages,
    LocalTime quietHoursStart,
    LocalTime quietHoursEnd
) {
    public static PreferenceResponse from(NotificationPreference pref) {
        return new PreferenceResponse(
            pref.getUserId(),
            pref.isEmailEnabled(),
            pref.isSmsEnabled(),
            pref.isPushEnabled(),
            pref.isInAppEnabled(),
            pref.isOrderUpdates(),
            pref.isPaymentNotifications(),
            pref.isDeliveryTracking(),
            pref.isBookingReminders(),
            pref.isPromotionalMessages(),
            pref.getQuietHoursStart(),
            pref.getQuietHoursEnd()
        );
    }
}
