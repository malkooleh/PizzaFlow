import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { notificationsApi } from "@/api/notifications.api";
import type { NotificationPreference } from "@/types/models";

// ── Query keys ───────────────────────────────────────────────────────────────

export const notificationKeys = {
  all: ["notifications"] as const,
  inbox: (userId: string) => [...notificationKeys.all, "inbox", userId] as const,
  unreadCount: (userId: string) => [...notificationKeys.all, "unread-count", userId] as const,
  preferences: (userId: string) => [...notificationKeys.all, "preferences", userId] as const,
};

// ── Queries ───────────────────────────────────────────────────────────────────

const NOTIFICATION_POLL_INTERVAL_MS = 30_000;

function requireUserId(userId: string | undefined): string {
  if (!userId) {
    throw new Error("User ID is required for notification operations");
  }
  return userId;
}

/**
 * Fetch inbox notifications with 30-second polling.
 * Backend: GET /api/v1/notifications/users/{userId}/inbox
 */
export function useInbox(userId: string | undefined) {
  return useQuery({
    queryKey: notificationKeys.inbox(userId ?? ""),
    queryFn: () => notificationsApi.getInbox(requireUserId(userId)),
    enabled: !!userId,
    staleTime: NOTIFICATION_POLL_INTERVAL_MS - 1000,
    refetchInterval: NOTIFICATION_POLL_INTERVAL_MS,
  });
}

/**
 * Fetch unread notification count with 30-second polling.
 * Backend: GET /api/v1/notifications/users/{userId}/unread-count
 */
export function useUnreadCount(userId: string | undefined) {
  return useQuery({
    queryKey: notificationKeys.unreadCount(userId ?? ""),
    queryFn: () => notificationsApi.getUnreadCount(requireUserId(userId)),
    enabled: !!userId,
    staleTime: NOTIFICATION_POLL_INTERVAL_MS - 1000,
    refetchInterval: NOTIFICATION_POLL_INTERVAL_MS,
  });
}

/**
 * Fetch notification preferences.
 * Backend: GET /api/v1/preferences/users/{userId}
 */
export function usePreferences(userId: string | undefined) {
  return useQuery({
    queryKey: notificationKeys.preferences(userId ?? ""),
    queryFn: () => notificationsApi.getPreferences(requireUserId(userId)),
    enabled: !!userId,
    staleTime: 60_000,
  });
}

// ── Mutations ────────────────────────────────────────────────────────────────

/**
 * Mark a single notification as read.
 * Invalidates inbox and unread-count caches after success.
 */
export function useMarkAsRead(userId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (notificationId: string) => {
      const safeUserId = requireUserId(userId);
      return notificationsApi.markAsRead(safeUserId, notificationId);
    },
    onSuccess: async () => {
      if (!userId) return;
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: notificationKeys.inbox(userId) }),
        queryClient.invalidateQueries({ queryKey: notificationKeys.unreadCount(userId) }),
      ]);
    },
  });
}

/**
 * Mark all notifications as read.
 * Invalidates inbox and unread-count caches after success.
 */
export function useMarkAllAsRead(userId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => {
      const safeUserId = requireUserId(userId);
      return notificationsApi.markAllAsRead(safeUserId);
    },
    onSuccess: async () => {
      if (!userId) return;
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: notificationKeys.inbox(userId) }),
        queryClient.invalidateQueries({ queryKey: notificationKeys.unreadCount(userId) }),
      ]);
    },
  });
}

/**
 * Update notification preferences.
 * Invalidates the preferences cache after success.
 */
export function useUpdatePreferences(userId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<NotificationPreference>) => {
      const safeUserId = requireUserId(userId);
      return notificationsApi.updatePreferences(safeUserId, data);
    },
    onSuccess: async () => {
      if (!userId) return;
      await queryClient.invalidateQueries({ queryKey: notificationKeys.preferences(userId) });
    },
  });
}

// ── Plan-compatible aliases ───────────────────────────────────────────────────
// The implementation plan names these hooks useNotifications and
// useNotificationPreferences; export aliases so both naming conventions work.

/** @alias useInbox — plan-compatible name for the inbox query */
export const useNotifications = useInbox;

/** @alias usePreferences — plan-compatible name for the preferences query */
export const useNotificationPreferences = usePreferences;
