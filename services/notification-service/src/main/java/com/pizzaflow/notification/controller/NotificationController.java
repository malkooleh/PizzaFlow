package com.pizzaflow.notification.controller;

import com.pizzaflow.notification.dto.*;
import com.pizzaflow.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Send a notification to a user.
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
        @Valid @RequestBody SendNotificationRequest request
    ) {
        NotificationResponse response = notificationService.sendNotification(request);
        if (response == null) {
            return ResponseEntity.noContent().build();  // User disabled this notification type
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get notification history for a user.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
        @PathVariable UUID userId,
        Pageable pageable
    ) {
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread in-app notifications.
     */
    @GetMapping("/users/{userId}/inbox")
    public ResponseEntity<List<InAppNotificationResponse>> getInbox(@PathVariable UUID userId) {
        List<InAppNotificationResponse> notifications = notificationService.getUnreadInAppNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count.
     */
    @GetMapping("/users/{userId}/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable UUID userId) {
        int count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Mark a notification as read.
     */
    @PutMapping("/users/{userId}/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
        @PathVariable UUID userId,
        @PathVariable UUID notificationId
    ) {
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read.
     */
    @PutMapping("/users/{userId}/read-all")
    public ResponseEntity<Integer> markAllAsRead(@PathVariable UUID userId) {
        int updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Archive (soft delete) notifications.
     */
    @PostMapping("/users/{userId}/archive")
    public ResponseEntity<Integer> archiveNotifications(
        @PathVariable UUID userId,
        @RequestBody List<UUID> notificationIds
    ) {
        int archived = notificationService.archiveNotifications(userId, notificationIds);
        return ResponseEntity.ok(archived);
    }
}
