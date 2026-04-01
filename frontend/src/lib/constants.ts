import type { UserRole } from "@/types/enums";

/** All API traffic goes through the gateway. Dev uses Vite proxy (empty string). */
export const API_BASE_URL = import.meta.env.VITE_API_URL;

/** WebSocket endpoint for kitchen KDS. Configure via VITE_KITCHEN_WS_URL env var. */
export const KITCHEN_WS_URL =
  (import.meta.env.VITE_KITCHEN_WS_URL as string | undefined) ??
  "ws://localhost:8084/ws/kitchen";

/** Mapbox public token */
export const MAPBOX_TOKEN = import.meta.env.VITE_MAPBOX_TOKEN as string;

/** Default request timeout in milliseconds. */
export const REQUEST_TIMEOUT_MS = 15_000;

/** Polling interval for active order status updates. */
export const ORDER_POLLING_INTERVAL_MS = 5_000;

/** Polling interval for unread notification count. */
export const NOTIFICATION_POLL_INTERVAL_MS = 30_000;

/** Roles in order of privilege for display. */
export const ROLE_LABELS: Record<UserRole, string> = {
  CUSTOMER: "Customer",
  KITCHEN_STAFF: "Kitchen Staff",
  COURIER: "Courier",
  RESTAURANT_MANAGER: "Restaurant Manager",
  SYSTEM_ADMIN: "System Admin",
};

/** Color classes for order status badges. */
export const ORDER_STATUS_COLORS: Record<string, string> = {
  PENDING: "bg-yellow-100 text-yellow-800",
  CONFIRMED: "bg-blue-100 text-blue-800",
  PREPARING: "bg-orange-100 text-orange-800",
  READY: "bg-green-100 text-green-800",
  PICKED_UP: "bg-purple-100 text-purple-800",
  DELIVERED: "bg-gray-100 text-gray-800",
  COMPLETED: "bg-gray-100 text-gray-800",
  CANCELLED: "bg-red-100 text-red-800",
};

/** Color classes for payment status badges. */
export const PAYMENT_STATUS_COLORS: Record<string, string> = {
  PENDING: "bg-yellow-100 text-yellow-800",
  PROCESSING: "bg-blue-100 text-blue-800",
  COMPLETED: "bg-green-100 text-green-800",
  FAILED: "bg-red-100 text-red-800",
  REFUNDED: "bg-gray-100 text-gray-800",
};

/** KDS timer thresholds in minutes */
export const KDS_TIMER_THRESHOLDS = {
  GREEN_MAX: 10,
  YELLOW_MAX: 20,
  // above RED_MIN = red
} as const;
