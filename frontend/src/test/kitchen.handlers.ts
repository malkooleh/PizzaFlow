import { http, HttpResponse } from "msw";
import { KitchenOrderStatus, OrderPriority } from "@/types/enums";
import type { KitchenOrderDTO, QueueStatusDTO } from "@/types/models";

const BASE = "/api/v1/kitchen";

// ── Fixtures ──────────────────────────────────────────────────────────────────

export function makeKitchenOrder(
  orderId: number,
  status: KitchenOrderStatus,
  overrides: Partial<KitchenOrderDTO> = {}
): KitchenOrderDTO {
  return {
    id: `uuid-${orderId}`,
    orderId,
    orderNumber: `PF-0${orderId}`,
    restaurantId: 1,
    customerId: 100,
    orderType: "DELIVERY",
    status,
    priority: OrderPriority.NORMAL,
    items: [
      {
        menuItemId: "menu-1",
        menuItemName: "Margherita",
        quantity: 2,
        specialInstructions: undefined,
      },
    ],
    estimatedPrepTimeMinutes: 15,
    queuePosition: 1,
    receivedAt: new Date().toISOString(),
    ...overrides,
  };
}

export const MOCK_KITCHEN_ORDERS: KitchenOrderDTO[] = [
  makeKitchenOrder(1001, KitchenOrderStatus.RECEIVED),
  makeKitchenOrder(1002, KitchenOrderStatus.PREPARING),
  makeKitchenOrder(1003, KitchenOrderStatus.READY),
];

export function makeQueueStatus(
  restaurantId = 1,
  orders: KitchenOrderDTO[] = MOCK_KITCHEN_ORDERS
): QueueStatusDTO {
  return {
    restaurantId,
    totalOrders: orders.length,
    receivedCount: orders.filter((o) => o.status === KitchenOrderStatus.RECEIVED).length,
    preparingCount: orders.filter((o) => o.status === KitchenOrderStatus.PREPARING).length,
    readyCount: orders.filter((o) => o.status === KitchenOrderStatus.READY).length,
    averageWaitTimeMinutes: 12,
    orders,
  };
}

// ── Handlers ──────────────────────────────────────────────────────────────────

const mockTimestamp = () => new Date().toISOString();

export const kitchenHandlers = [
  // GET /api/v1/kitchen/queue/:restaurantId
  http.get(`${BASE}/queue/:restaurantId`, ({ params }) => {
    const restaurantId = Number(params.restaurantId);
    return HttpResponse.json({
      success: true,
      data: makeQueueStatus(restaurantId),
      message: "OK",
      timestamp: mockTimestamp(),
    });
  }),

  // GET /api/v1/kitchen/orders/:orderId
  http.get(`${BASE}/orders/:orderId`, ({ params }) => {
    const orderId = Number(params.orderId);
    const order =
      MOCK_KITCHEN_ORDERS.find((o) => o.orderId === orderId) ??
      makeKitchenOrder(orderId, KitchenOrderStatus.RECEIVED);
    return HttpResponse.json({
      success: true,
      data: order,
      message: "OK",
      timestamp: mockTimestamp(),
    });
  }),

  // POST /api/v1/kitchen/orders/:orderId/start
  http.post(`${BASE}/orders/:orderId/start`, ({ params }) => {
    const orderId = Number(params.orderId);
    const base =
      MOCK_KITCHEN_ORDERS.find((o) => o.orderId === orderId) ??
      makeKitchenOrder(orderId, KitchenOrderStatus.RECEIVED);
    const updated: KitchenOrderDTO = {
      ...base,
      status: KitchenOrderStatus.PREPARING,
      startedAt: mockTimestamp(),
    };
    return HttpResponse.json({
      success: true,
      data: updated,
      message: "Order preparation started",
      timestamp: mockTimestamp(),
    });
  }),

  // POST /api/v1/kitchen/orders/:orderId/ready
  http.post(`${BASE}/orders/:orderId/ready`, ({ params }) => {
    const orderId = Number(params.orderId);
    const base =
      MOCK_KITCHEN_ORDERS.find((o) => o.orderId === orderId) ??
      makeKitchenOrder(orderId, KitchenOrderStatus.PREPARING);
    const updated: KitchenOrderDTO = {
      ...base,
      status: KitchenOrderStatus.READY,
    };
    return HttpResponse.json({
      success: true,
      data: updated,
      message: "Order marked as ready",
      timestamp: mockTimestamp(),
    });
  }),

  // POST /api/v1/kitchen/orders/:orderId/pickup
  http.post(`${BASE}/orders/:orderId/pickup`, ({ params }) => {
    const orderId = Number(params.orderId);
    const base =
      MOCK_KITCHEN_ORDERS.find((o) => o.orderId === orderId) ??
      makeKitchenOrder(orderId, KitchenOrderStatus.READY);
    const updated: KitchenOrderDTO = {
      ...base,
      status: KitchenOrderStatus.PICKED_UP,
      completedAt: mockTimestamp(),
    };
    return HttpResponse.json({
      success: true,
      data: updated,
      message: "Order picked up",
      timestamp: mockTimestamp(),
    });
  }),
];
