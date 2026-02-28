package com.pizzaflow.notification.controller;

import com.pizzaflow.notification.dto.PreferenceResponse;
import com.pizzaflow.notification.dto.UpdatePreferenceRequest;
import com.pizzaflow.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/preferences")
@Tag(name = "Notification Preferences", description = "Manage per-user channel preferences (email, SMS, push, in-app) for each notification type")
public class PreferenceController {

    private final NotificationService notificationService;

    public PreferenceController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get user's notification preferences.
     */
    @Operation(summary = "Get notification preferences for a user")
    @GetMapping("/users/{userId}")
    public ResponseEntity<PreferenceResponse> getPreferences(@PathVariable UUID userId) {
        PreferenceResponse preferences = notificationService.getPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Update user's notification preferences.
     */
    @Operation(summary = "Update notification preferences for a user")
    @PutMapping("/users/{userId}")
    public ResponseEntity<PreferenceResponse> updatePreferences(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdatePreferenceRequest request) {
        PreferenceResponse preferences = notificationService.updatePreferences(userId, request);
        return ResponseEntity.ok(preferences);
    }
}
