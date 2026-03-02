import { renderHook, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw";
import { server } from "@/test/handlers";
import { makeQueryWrapper } from "@/test/render-helpers";
import { KitchenOrderStatus } from "@/types/enums";
import {
  makeKitchenOrder,
  makeQueueStatus,
} from "@/test/kitchen.handlers";
import {
  useKitchenQueue,
  useStartPreparing,
  useMarkReady,
  useMarkPickedUp,
} from "../use-kitchen";

const BASE = "/api/v1/kitchen";

describe("use-kitchen hooks", () => {
  describe("useKitchenQueue", () => {
    it("fetches the queue status for a restaurant", async () => {
      const queueStatus = makeQueueStatus(1);
      server.use(
        http.get(`${BASE}/queue/:restaurantId`, () =>
          HttpResponse.json({
            success: true,
            data: queueStatus,
            message: "OK",
            timestamp: new Date().toISOString(),
          })
        )
      );

      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useKitchenQueue(1), {
        wrapper: Wrapper,
      });

      await waitFor(() => expect(result.current.isSuccess).toBe(true));
      expect(result.current.data?.restaurantId).toBe(1);
      expect(result.current.data?.orders).toHaveLength(3);
    });

    it("is disabled when restaurantId is undefined", () => {
      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useKitchenQueue(undefined), {
        wrapper: Wrapper,
      });
      expect(result.current.fetchStatus).toBe("idle");
    });
  });

  describe("useStartPreparing", () => {
    it("calls the start endpoint and returns the updated order", async () => {
      const receivedOrder = makeKitchenOrder(2001, KitchenOrderStatus.RECEIVED);
      const preparingOrder = {
        ...receivedOrder,
        status: KitchenOrderStatus.PREPARING,
      };

      server.use(
        http.post(`${BASE}/orders/:orderId/start`, () =>
          HttpResponse.json({
            success: true,
            data: preparingOrder,
            message: "Order preparation started",
            timestamp: new Date().toISOString(),
          })
        )
      );

      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useStartPreparing(), {
        wrapper: Wrapper,
      });

      const data = await result.current.mutateAsync({ orderId: 2001 });
      expect(data.status).toBe(KitchenOrderStatus.PREPARING);
    });
  });

  describe("useMarkReady", () => {
    it("calls the ready endpoint", async () => {
      const readyOrder = makeKitchenOrder(2002, KitchenOrderStatus.READY);

      server.use(
        http.post(`${BASE}/orders/:orderId/ready`, () =>
          HttpResponse.json({
            success: true,
            data: readyOrder,
            message: "Order marked as ready",
            timestamp: new Date().toISOString(),
          })
        )
      );

      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useMarkReady(), { wrapper: Wrapper });

      const data = await result.current.mutateAsync({ orderId: 2002 });
      expect(data.status).toBe(KitchenOrderStatus.READY);
    });
  });

  describe("useMarkPickedUp", () => {
    it("calls the pickup endpoint", async () => {
      const pickedUpOrder = makeKitchenOrder(2003, KitchenOrderStatus.PICKED_UP);

      server.use(
        http.post(`${BASE}/orders/:orderId/pickup`, () =>
          HttpResponse.json({
            success: true,
            data: pickedUpOrder,
            message: "Order picked up",
            timestamp: new Date().toISOString(),
          })
        )
      );

      const { Wrapper } = makeQueryWrapper();
      const { result } = renderHook(() => useMarkPickedUp(), {
        wrapper: Wrapper,
      });

      const data = await result.current.mutateAsync({ orderId: 2003 });
      expect(data.status).toBe(KitchenOrderStatus.PICKED_UP);
    });
  });
});
