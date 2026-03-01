import { http, HttpResponse } from "msw";
import { OrderStatus, OrderType, PaymentStatus, PaymentMethodType } from "@/types/enums";

const BASE = "/api/v1";

// ── Mock data ─────────────────────────────────────────────────────────────────

let orderIdCounter = 1001;

function makeOrder(overrides: Record<string, unknown> = {}) {
  const id = orderIdCounter++;
  return {
    id,
    orderNumber: `PF-${String(id).padStart(5, "0")}`,
    customerId: 1,
    restaurantId: "rest-1",
    orderType: OrderType.DELIVERY,
    status: OrderStatus.PENDING,
    deliveryAddress: "123 Main St, New York, 10001",
    items: [
      {
        id: 1,
        menuItemId: "item-1",
        menuItemName: "Margherita",
        quantity: 1,
        unitPrice: 12.99,
        specialInstructions: null,
      },
    ],
    subtotal: 12.99,
    tax: 1.30,
    deliveryFee: 2.99,
    totalAmount: 17.28,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    ...overrides,
  };
}

const orderStore: ReturnType<typeof makeOrder>[] = [
  makeOrder({ status: OrderStatus.PREPARING, orderType: OrderType.DELIVERY }),
  makeOrder({
    status: OrderStatus.COMPLETED,
    orderType: OrderType.PICKUP,
    items: [
      { id: 1, menuItemId: "item-2", menuItemName: "Pepperoni", quantity: 2, unitPrice: 14.99, specialInstructions: null },
    ],
    subtotal: 29.98,
    tax: 3.00,
    deliveryFee: 0,
    totalAmount: 32.98,
    createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
  }),
  makeOrder({
    status: OrderStatus.CANCELLED,
    orderType: OrderType.DELIVERY,
    createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
  }),
];

// ── Handlers ──────────────────────────────────────────────────────────────────

export const ordersHandlers = [
  // POST /api/v1/orders — create order
  http.post(`${BASE}/orders`, async ({ request }) => {
    const body = await request.json() as Record<string, unknown>;
    const rawItems = body.items as { menuItemId: string; menuItemName?: string; quantity: number; unitPrice?: number }[];
    const order = makeOrder({
      customerId: body.customerId,
      restaurantId: body.restaurantId,
      orderType: body.orderType,
      deliveryAddress: body.deliveryAddress,
      tableNumber: body.tableNumber,
      scheduledTime: body.scheduledTime,
      status: OrderStatus.PENDING,
      items: rawItems.map((item, i) => ({
        id: i + 1,
        menuItemId: item.menuItemId,
        menuItemName: item.menuItemName ?? `Item ${item.menuItemId}`,
        quantity: item.quantity,
        unitPrice: item.unitPrice ?? 12.99,
      })),
    });
    orderStore.push(order);
    return HttpResponse.json({ success: true, data: order, message: "Order created", timestamp: new Date().toISOString() });
  }),

  // GET /api/v1/orders/customer/:customerId — list orders by customer
  http.get(`${BASE}/orders/customer/:customerId`, () => {
    return HttpResponse.json({ success: true, data: orderStore, message: "OK", timestamp: new Date().toISOString() });
  }),

  // GET /api/v1/orders/number/:orderNumber — get order by order number
  http.get(`${BASE}/orders/number/:orderNumber`, ({ params }) => {
    const order =
      orderStore.find((o) => o.orderNumber === params.orderNumber) ??
      makeOrder({ orderNumber: params.orderNumber });
    return HttpResponse.json({ success: true, data: order, message: "OK", timestamp: new Date().toISOString() });
  }),

  // GET /api/v1/orders/:id — get single order
  http.get(`${BASE}/orders/:id`, ({ params }) => {
    const id = Number(params.id);
    const order = orderStore.find((o) => o.id === id) ?? makeOrder({ id });
    return HttpResponse.json({ success: true, data: order, message: "OK", timestamp: new Date().toISOString() });
  }),

  // POST /api/v1/orders/:id/cancel
  http.post(`${BASE}/orders/:id/cancel`, ({ params }) => {
    const id = Number(params.id);
    const order = orderStore.find((o) => o.id === id);
    if (order) order.status = OrderStatus.CANCELLED;
    const result = order ?? makeOrder({ id, status: OrderStatus.CANCELLED });
    return HttpResponse.json({ success: true, data: result, message: "Order cancelled", timestamp: new Date().toISOString() });
  }),

  // POST /api/v1/payments
  http.post(`${BASE}/payments`, async ({ request }) => {
    const body = await request.json() as Record<string, unknown>;
    const payment = {
      transactionId: `txn-${Date.now()}`,
      orderId: body.orderId,
      customerId: body.customerId ?? 1,
      amount: body.amount ?? 17.28,
      currency: body.currency ?? "USD",
      status: PaymentStatus.COMPLETED,
      paymentMethodType: body.paymentMethodType ?? PaymentMethodType.CREDIT_CARD,
      gatewayTransactionId: `gw-${Date.now()}`,
      createdAt: new Date().toISOString(),
    };
    return HttpResponse.json({ success: true, data: payment, message: "Payment processed", timestamp: new Date().toISOString() });
  }),

  // GET /api/v1/payments/order/:orderId
  http.get(`${BASE}/payments/order/:orderId`, ({ params }) => {
    const payment = {
      transactionId: `txn-${params.orderId}`,
      orderId: Number(params.orderId),
      customerId: 1,
      amount: 17.28,
      currency: "USD",
      status: PaymentStatus.COMPLETED,
      paymentMethodType: PaymentMethodType.CREDIT_CARD,
      createdAt: new Date().toISOString(),
    };
    return HttpResponse.json({ success: true, data: payment, message: "OK", timestamp: new Date().toISOString() });
  }),

  // GET /api/v1/payments/customer/:customerId
  http.get(`${BASE}/payments/customer/:customerId`, () => {
    return HttpResponse.json({ success: true, data: [], message: "OK", timestamp: new Date().toISOString() });
  }),
];
