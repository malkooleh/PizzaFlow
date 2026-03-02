import { api } from "./client";
import type { ApiResponse } from "./types";
import { unwrap } from "./types";
import type { KitchenOrderDTO, QueueStatusDTO } from "@/types/models";

// ── API ──────────────────────────────────────────────────────────────────────

export const kitchenApi = {
  /**
   * Get the full queue status for a restaurant, including all active orders.
   * Backend: GET /api/v1/kitchen/queue/{restaurantId}
   */
  getQueueStatus: (restaurantId: number) =>
    api
      .get(`api/v1/kitchen/queue/${restaurantId}`)
      .json<ApiResponse<QueueStatusDTO>>()
      .then(unwrap),

  /**
   * Get a single kitchen order by its order ID.
   * Backend: GET /api/v1/kitchen/orders/{orderId}
   */
  getKitchenOrder: (orderId: number) =>
    api
      .get(`api/v1/kitchen/orders/${orderId}`)
      .json<ApiResponse<KitchenOrderDTO>>()
      .then(unwrap),

  /**
   * Transition an order from RECEIVED → PREPARING.
   * Backend: POST /api/v1/kitchen/orders/{orderId}/start?station={station}
   */
  startPreparing: (orderId: number, station?: string) =>
    api
      .post(`api/v1/kitchen/orders/${orderId}/start`, {
        searchParams: station ? { station } : {},
      })
      .json<ApiResponse<KitchenOrderDTO>>()
      .then(unwrap),

  /**
   * Transition an order from PREPARING → READY.
   * Backend: POST /api/v1/kitchen/orders/{orderId}/ready
   */
  markReady: (orderId: number) =>
    api
      .post(`api/v1/kitchen/orders/${orderId}/ready`)
      .json<ApiResponse<KitchenOrderDTO>>()
      .then(unwrap),

  /**
   * Transition an order from READY → PICKED_UP.
   * Backend: POST /api/v1/kitchen/orders/{orderId}/pickup
   */
  markPickedUp: (orderId: number) =>
    api
      .post(`api/v1/kitchen/orders/${orderId}/pickup`)
      .json<ApiResponse<KitchenOrderDTO>>()
      .then(unwrap),
};
