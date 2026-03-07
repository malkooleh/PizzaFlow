import { api } from "./client";
import type { InAppNotificationResponse, NotificationPreference } from "@/types/models";

/**
 * Notification service client.
 *
 * IMPORTANT: This service returns raw DTOs directly — NOT wrapped in
 * `ApiResponse<T>`. Do not call `.then(unwrap)` on these methods.
 */
export const notificationsApi = {
  /**
   * Fetch all in-app notifications for the authenticated user.
   * Backend: GET /api/v1/notifications/users/{userId}/inbox
   */
  getInbox: (userId: string) =>
    api.get(`api/v1/notifications/users/${userId}/inbox`).json<InAppNotificationResponse[]>(),

  /**
   * Get unread notification count for the authenticated user.
   * Backend: GET /api/v1/notifications/users/{userId}/unread-count
   */
  getUnreadCount: (userId: string) =>
    api.get(`api/v1/notifications/users/${userId}/unread-count`).json<number>(),

  /**
   * Mark a single notification as read.
   * Backend: PUT /api/v1/notifications/users/{userId}/notifications/{notificationId}/read
   */
  markAsRead: (userId: string, notificationId: string) =>
    api
      .put(`api/v1/notifications/users/${userId}/notifications/${notificationId}/read`)
      .json<void>(),

  /**
   * Mark all notifications as read for the user.
   * Backend: PUT /api/v1/notifications/users/{userId}/read-all
   */
  markAllAsRead: (userId: string) =>
    api.put(`api/v1/notifications/users/${userId}/read-all`).json<number>(),

  /**
   * Get notification preferences for the authenticated user.
   * Backend: GET /api/v1/preferences/users/{userId}
   */
  getPreferences: (userId: string) =>
    api.get(`api/v1/preferences/users/${userId}`).json<NotificationPreference>(),

  /**
   * Update notification preferences for the authenticated user.
   * Backend: PUT /api/v1/preferences/users/{userId}
   */
  updatePreferences: (userId: string, data: Partial<NotificationPreference>) =>
    api
      .put(`api/v1/preferences/users/${userId}`, { json: data })
      .json<NotificationPreference>(),
};
