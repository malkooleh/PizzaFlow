package com.pizzaflow.notification.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "email_enabled")
    private boolean emailEnabled = true;

    @Column(name = "sms_enabled")
    private boolean smsEnabled = true;

    @Column(name = "push_enabled")
    private boolean pushEnabled = true;

    @Column(name = "in_app_enabled")
    private boolean inAppEnabled = true;

    // Event-specific preferences
    @Column(name = "order_updates")
    private boolean orderUpdates = true;

    @Column(name = "payment_notifications")
    private boolean paymentNotifications = true;

    @Column(name = "delivery_tracking")
    private boolean deliveryTracking = true;

    @Column(name = "booking_reminders")
    private boolean bookingReminders = true;

    @Column(name = "promotional_messages")
    private boolean promotionalMessages = false;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }

    public boolean isSmsEnabled() { return smsEnabled; }
    public void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }

    public boolean isPushEnabled() { return pushEnabled; }
    public void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }

    public boolean isInAppEnabled() { return inAppEnabled; }
    public void setInAppEnabled(boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }

    public boolean isOrderUpdates() { return orderUpdates; }
    public void setOrderUpdates(boolean orderUpdates) { this.orderUpdates = orderUpdates; }

    public boolean isPaymentNotifications() { return paymentNotifications; }
    public void setPaymentNotifications(boolean paymentNotifications) { this.paymentNotifications = paymentNotifications; }

    public boolean isDeliveryTracking() { return deliveryTracking; }
    public void setDeliveryTracking(boolean deliveryTracking) { this.deliveryTracking = deliveryTracking; }

    public boolean isBookingReminders() { return bookingReminders; }
    public void setBookingReminders(boolean bookingReminders) { this.bookingReminders = bookingReminders; }

    public boolean isPromotionalMessages() { return promotionalMessages; }
    public void setPromotionalMessages(boolean promotionalMessages) { this.promotionalMessages = promotionalMessages; }

    public LocalTime getQuietHoursStart() { return quietHoursStart; }
    public void setQuietHoursStart(LocalTime quietHoursStart) { this.quietHoursStart = quietHoursStart; }

    public LocalTime getQuietHoursEnd() { return quietHoursEnd; }
    public void setQuietHoursEnd(LocalTime quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /**
     * Check if it's currently quiet hours for this user.
     */
    public boolean isQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
        } else {
            // Overnight quiet hours (e.g., 22:00 to 07:00)
            return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
        }
    }
}
