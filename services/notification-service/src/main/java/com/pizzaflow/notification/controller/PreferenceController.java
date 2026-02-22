package com.pizzaflow.notification.controller;

import com.pizzaflow.notification.dto.PreferenceResponse;
import com.pizzaflow.notification.dto.UpdatePreferenceRequest;
import com.pizzaflow.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    private final NotificationService notificationService;

    public PreferenceController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get user's notification preferences.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<PreferenceResponse> getPreferences(@PathVariable UUID userId) {
        PreferenceResponse preferences = notificationService.getPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Update user's notification preferences.
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<PreferenceResponse> updatePreferences(
        @PathVariable UUID userId,
        @Valid @RequestBody UpdatePreferenceRequest request
    ) {
        PreferenceResponse preferences = notificationService.updatePreferences(userId, request);
        return ResponseEntity.ok(preferences);
    }
}
