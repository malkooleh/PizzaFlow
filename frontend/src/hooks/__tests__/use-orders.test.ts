import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import { server } from "@/test/handlers";
import { makeQueryWrapper } from "@/test/render-helpers";
import { OrderStatus, OrderType } from "@/types/enums";
import {
  useOrders,
  useOrder,
  useCancelOrder,
  useActiveOrderTracking,
  ACTIVE_ORDER_STATUSES,
} from "../use-orders";
import { vi } from "vitest";

const BASE = "/api/v1";

function makeOrderFixture(id = 1001, status = OrderStatus.PENDING) {
  return {
    id,
    orderNumber: `PF-0${id}`,
    customerId: 1,
    restaurantId: 1,
    orderType: OrderType.DELIVERY,
    status,
    deliveryAddress: "1 Test St",
    items: [{ id: 1, menuItemId: "m1", menuItemName: "Margherita", quantity: 1, unitPrice: 12.99 }],
    subtotal: 12.99,
    tax: 1.30,
    deliveryFee: 2.99,
    totalAmount: 17.28,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
}

describe("use-orders hooks", () => {
  describe("useOrders", () => {
    it("returns the customer order list from the API", async () => {
      const order = makeOrderFixture(2001);
      server.use(
        http.get(`${BASE}/orders/customer/:customerId`, () =>
          HttpResponse.json({ success: true, data: [order], message: "OK", timestamp: new Date().toISOString() }),
        ),
      );

      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useOrders(1), { wrapper: Wrapper });

      await waitFor(() => expect(result.current.isSuccess).toBe(true));
      expect(result.current.data).toHaveLength(1);
      expect(result.current.data?.[0].id).toBe(2001);
    });

    it("is disabled when customerId is undefined", () => {
      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useOrders(undefined), { wrapper: Wrapper });
      expect(result.current.fetchStatus).toBe("idle");
    });
  });

  describe("useOrder", () => {
    it("fetches a single order by id", async () => {
      const order = makeOrderFixture(3001, OrderStatus.CONFIRMED);
      server.use(
        http.get(`${BASE}/orders/:id`, () =>
          HttpResponse.json({ success: true, data: order, message: "OK", timestamp: new Date().toISOString() }),
        ),
      );

      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useOrder(3001), { wrapper: Wrapper });

      await waitFor(() => expect(result.current.isSuccess).toBe(true));
      expect(result.current.data?.status).toBe(OrderStatus.CONFIRMED);
    });

    it("is disabled when id is undefined", () => {
      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useOrder(undefined), { wrapper: Wrapper });
      expect(result.current.fetchStatus).toBe("idle");
    });
  });

  describe("useCancelOrder", () => {
    it("calls the cancel endpoint and returns without error on null data", async () => {
      // Backend returns ApiResponse<Void> — data IS null on success
      server.use(
        http.post(`${BASE}/orders/:id/cancel`, () =>
          HttpResponse.json({ success: true, data: null, message: "Order cancelled", timestamp: new Date().toISOString() }),
        ),
      );

      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useCancelOrder(), { wrapper: Wrapper });

      await result.current.mutateAsync({ id: 1001 });
      await waitFor(() => expect(result.current.isSuccess).toBe(true));
    });

    it("throws if success is false", async () => {
      server.use(
        http.post(`${BASE}/orders/:id/cancel`, () =>
          HttpResponse.json({ success: false, data: null, error: "Cannot cancel order in current state", message: null, timestamp: new Date().toISOString() }),
        ),
      );

      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useCancelOrder(), { wrapper: Wrapper });

      await expect(result.current.mutateAsync({ id: 1001 })).rejects.toThrow("Cannot cancel order");
    });
  });

  describe("useActiveOrderTracking", () => {
    it("polls while order is in ACTIVE_ORDER_STATUSES", async () => {
      const preparingOrder = makeOrderFixture(4001, OrderStatus.PREPARING);
      expect(ACTIVE_ORDER_STATUSES.has(preparingOrder.status)).toBe(true);

      server.use(
        http.get(`${BASE}/orders/:id`, () =>
          HttpResponse.json({ success: true, data: preparingOrder, message: "OK", timestamp: new Date().toISOString() }),
        ),
      );

      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useActiveOrderTracking(4001), { wrapper: Wrapper });

      await waitFor(() => expect(result.current.isSuccess).toBe(true));
      expect(result.current.data?.status).toBe(OrderStatus.PREPARING);
    });

    it("does not poll for COMPLETED orders", async () => {
      const completedOrder = makeOrderFixture(4002, OrderStatus.COMPLETED);
      expect(ACTIVE_ORDER_STATUSES.has(completedOrder.status)).toBe(false);

      const fetchSpy = vi.fn().mockReturnValue(
        HttpResponse.json({ success: true, data: completedOrder, message: "OK", timestamp: new Date().toISOString() }),
      );
      server.use(http.get(`${BASE}/orders/:id`, fetchSpy));

      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useActiveOrderTracking(4002), { wrapper: Wrapper });

      await waitFor(() => expect(result.current.isSuccess).toBe(true));
      // refetchInterval callback should return false for completed orders
      const callsBefore = fetchSpy.mock.calls.length;
      await new Promise((r) => setTimeout(r, 50));
      expect(fetchSpy.mock.calls.length).toBe(callsBefore); // no additional polls
    });
  });

  describe("ACTIVE_ORDER_STATUSES", () => {
    it("contains expected in-progress statuses", () => {
      expect(ACTIVE_ORDER_STATUSES.has(OrderStatus.PENDING)).toBe(true);
      expect(ACTIVE_ORDER_STATUSES.has(OrderStatus.PREPARING)).toBe(true);
      expect(ACTIVE_ORDER_STATUSES.has(OrderStatus.OUT_FOR_DELIVERY)).toBe(true);
    });

    it("does not contain terminal statuses", () => {
      expect(ACTIVE_ORDER_STATUSES.has(OrderStatus.COMPLETED)).toBe(false);
      expect(ACTIVE_ORDER_STATUSES.has(OrderStatus.DELIVERED)).toBe(false);
      expect(ACTIVE_ORDER_STATUSES.has(OrderStatus.CANCELLED)).toBe(false);
    });
  });
});
