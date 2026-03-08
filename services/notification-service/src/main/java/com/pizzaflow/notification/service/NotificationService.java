package com.pizzaflow.notification.service;

import com.pizzaflow.notification.dto.*;
import com.pizzaflow.notification.model.InAppNotification;
import com.pizzaflow.notification.model.Notification;
import com.pizzaflow.notification.model.NotificationPreference;
import com.pizzaflow.notification.model.NotificationTemplate;
import com.pizzaflow.notification.model.enums.NotificationChannel;
import com.pizzaflow.notification.model.enums.NotificationPriority;
import com.pizzaflow.notification.model.enums.NotificationStatus;
import com.pizzaflow.notification.repository.InAppNotificationRepository;
import com.pizzaflow.notification.repository.NotificationPreferenceRepository;
import com.pizzaflow.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final InAppNotificationRepository inAppRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final TemplateService templateService;
    private final EmailService emailService;

    public NotificationService(
        NotificationRepository notificationRepository,
        InAppNotificationRepository inAppRepository,
        NotificationPreferenceRepository preferenceRepository,
        TemplateService templateService,
        EmailService emailService
    ) {
        this.notificationRepository = notificationRepository;
        this.inAppRepository = inAppRepository;
        this.preferenceRepository = preferenceRepository;
        this.templateService = templateService;
        this.emailService = emailService;
    }

    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("Sending {} notification to user {}", request.channel(), request.userId());

        // Check user preferences
        Optional<NotificationPreference> prefs = preferenceRepository.findByUserId(request.userId());
        if (prefs.isPresent() && !isChannelEnabled(prefs.get(), request.channel())) {
            log.info("User {} has disabled {} notifications", request.userId(), request.channel());
            return null;
        }

        // Prepare content
        String subject = request.subject();
        String body = request.body();

        // Use template if specified
        if (request.templateName() != null) {
            Optional<NotificationTemplate> template = templateService.getTemplate(request.templateName());
            if (template.isPresent()) {
                TemplateService.RenderedTemplate rendered = templateService.render(
                    template.get(), request.variables()
                );
                subject = rendered.subject();
                body = rendered.body();
            }
        }

        // Create notification record
        Notification notification = new Notification();
        notification.setUserId(request.userId());
        notification.setChannel(request.channel());
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setRecipient(request.recipient());
        notification.setEventType(request.eventType());
        notification.setReferenceId(request.referenceId());
        notification.setReferenceType(request.referenceType());
        notification.setMetadata(request.metadata());
        notification.setStatus(NotificationStatus.PENDING);

        notification = notificationRepository.save(notification);

        // Send based on channel
        boolean sent = switch (request.channel()) {
            case EMAIL -> sendEmail(notification);
            case SMS -> sendSms(notification);
            case PUSH -> sendPush(notification);
            case IN_APP -> sendInApp(notification, request.priority());
        };

        if (sent) {
            notification.markSent();
        } else {
            notification.markFailed("Failed to send through channel");
        }

        notification = notificationRepository.save(notification);
        return NotificationResponse.from(notification);
    }

    private boolean sendEmail(Notification notification) {
        try {
            emailService.sendEmail(
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody()
            );
            return true;
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendSms(Notification notification) {
        // SMS implementation would go here (Twilio, etc.)
        log.info("SMS sending not implemented. Would send to: {}", notification.getRecipient());
        return true;  // Mock success
    }

    private boolean sendPush(Notification notification) {
        // Push notification implementation would go here (Firebase, etc.)
        log.info("Push notification not implemented. Would send to: {}", notification.getRecipient());
        return true;  // Mock success
    }

    @Transactional
    private boolean sendInApp(Notification notification, NotificationPriority priority) {
        InAppNotification inApp = new InAppNotification();
        inApp.setUserId(notification.getUserId());
        inApp.setTitle(notification.getSubject());
        inApp.setMessage(notification.getBody());
        inApp.setEventType(notification.getEventType());
        inApp.setReferenceId(notification.getReferenceId());
        inApp.setReferenceType(notification.getReferenceType());
        inApp.setPriority(priority != null ? priority : NotificationPriority.NORMAL);

        inAppRepository.save(inApp);
        log.info("Created in-app notification for user {}", notification.getUserId());
        return true;
    }

    private boolean isChannelEnabled(NotificationPreference pref, NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> pref.isEmailEnabled();
            case SMS -> pref.isSmsEnabled();
            case PUSH -> pref.isPushEnabled();
            case IN_APP -> pref.isInAppEnabled();
        };
    }

    // ========== User Notifications ==========

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<InAppNotificationResponse> getUnreadInAppNotifications(UUID userId, Pageable pageable) {
        return inAppRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
            .map(InAppNotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(UUID userId) {
        return inAppRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        inAppRepository.findById(notificationId)
            .filter(n -> n.getUserId().equals(userId))
            .ifPresent(n -> {
                n.setRead(true);
                inAppRepository.save(n);
            });
    }

    @Transactional
    public int markAllAsRead(UUID userId) {
        return inAppRepository.markAllAsRead(userId);
    }

    @Transactional
    public int archiveNotifications(UUID userId, List<UUID> ids) {
        return inAppRepository.archiveNotifications(userId, ids);
    }

    // ========== Preferences ==========

    @Transactional(readOnly = true)
    public PreferenceResponse getPreferences(UUID userId) {
        return preferenceRepository.findByUserId(userId)
            .map(PreferenceResponse::from)
            .orElseGet(() -> PreferenceResponse.from(createDefaultPreferences(userId)));
    }

    @Transactional
    public PreferenceResponse updatePreferences(UUID userId, UpdatePreferenceRequest request) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreferences(userId));

        if (request.emailEnabled() != null) pref.setEmailEnabled(request.emailEnabled());
        if (request.smsEnabled() != null) pref.setSmsEnabled(request.smsEnabled());
        if (request.pushEnabled() != null) pref.setPushEnabled(request.pushEnabled());
        if (request.inAppEnabled() != null) pref.setInAppEnabled(request.inAppEnabled());
        if (request.orderUpdates() != null) pref.setOrderUpdates(request.orderUpdates());
        if (request.paymentNotifications() != null) pref.setPaymentNotifications(request.paymentNotifications());
        if (request.deliveryTracking() != null) pref.setDeliveryTracking(request.deliveryTracking());
        if (request.bookingReminders() != null) pref.setBookingReminders(request.bookingReminders());
        if (request.promotionalMessages() != null) pref.setPromotionalMessages(request.promotionalMessages());
        if (request.quietHoursStart() != null) pref.setQuietHoursStart(request.quietHoursStart());
        if (request.quietHoursEnd() != null) pref.setQuietHoursEnd(request.quietHoursEnd());

        pref = preferenceRepository.save(pref);
        return PreferenceResponse.from(pref);
    }

    private NotificationPreference createDefaultPreferences(UUID userId) {
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(userId);
        return preferenceRepository.save(pref);
    }
}
