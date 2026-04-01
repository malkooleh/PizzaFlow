import { api } from "./client";
import type { ApiResponse } from "./types";
import { unwrap, unwrapVoid } from "./types";
import type { OrderResponse } from "@/types/models";
import type { OrderType } from "@/types/enums";

// ── Request shapes ───────────────────────────────────────────────────────────

export interface CreateOrderItemRequest {
  menuItemId: string;
  /** Required by backend — must match the catalog item name */
  menuItemName: string;
  quantity: number;
  /** Required by backend — unit price at time of order */
  unitPrice: number;
  specialInstructions?: string;
}

export interface CreateOrderRequest {
  /** Numeric database ID resolved from the Keycloak JWT */
  customerId: number;
  /** Numeric database restaurant ID */
  restaurantId: string | number;
  orderType: OrderType;
  items: CreateOrderItemRequest[];
  deliveryAddress?: string;
  tableNumber?: string;
  scheduledTime?: string;         // ISO-8601
  specialInstructions?: string;
}

// ── API ──────────────────────────────────────────────────────────────────────

export const ordersApi = {
  /**
   * Place a new order.
   * Backend: POST /api/v1/orders
   */
  createOrder: (request: CreateOrderRequest) =>
    api
      .post("api/v1/orders", { json: request })
      .json<ApiResponse<OrderResponse>>()
      .then(unwrap),

  /**
   * Get all orders for a specific customer.
   * Backend: GET /api/v1/orders/customer/{customerId}
   */
  getOrdersByCustomer: (customerId: number) =>
    api
      .get(`api/v1/orders/customer/${customerId}`)
      .json<ApiResponse<OrderResponse[]>>()
      .then(unwrap),

  /**
   * Get a single order by ID.
   * Backend: GET /api/v1/orders/{orderId}
   */
  getOrder: (id: number) =>
    api
      .get(`api/v1/orders/${id}`)
      .json<ApiResponse<OrderResponse>>()
      .then(unwrap),

  /**
   * Get order by order number.
   * Backend: GET /api/v1/orders/number/{orderNumber}
   */
  getOrderByNumber: (orderNumber: string) =>
    api
      .get(`api/v1/orders/number/${orderNumber}`)
      .json<ApiResponse<OrderResponse>>()
      .then(unwrap),

  /**
   * Cancel an order.
   * Backend: POST /api/v1/orders/{orderId}/cancel?reason={reason}
   */
  cancelOrder: (id: number, reason?: string) =>
    api
      .post(`api/v1/orders/${id}/cancel`, {
        searchParams: reason ? { reason } : {},
      })
      .json<ApiResponse<void | null>>()
      .then(unwrapVoid),
};
