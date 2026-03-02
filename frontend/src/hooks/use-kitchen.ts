import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { kitchenApi } from "@/api/kitchen.api";
import { createKitchenWebSocket } from "@/lib/websocket";
import { ORDER_POLLING_INTERVAL_MS } from "@/lib/constants";
import type { QueueStatusDTO } from "@/types/models";

// ── Query keys ───────────────────────────────────────────────────────────────

export const kitchenKeys = {
  all: ["kitchen"] as const,
  queue: (restaurantId: number) => [...kitchenKeys.all, "queue", restaurantId] as const,
  order: (orderId: number) => [...kitchenKeys.all, "order", orderId] as const,
};

// ── Queries ───────────────────────────────────────────────────────────────────

/**
 * Fetch and auto-poll the kitchen queue for a restaurant every 5 seconds.
 * WebSocket updates take precedence; polling is a fallback safety net.
 * Backend: GET /api/v1/kitchen/queue/{restaurantId}
 */
export function useKitchenQueue(restaurantId: number | undefined) {
  return useQuery({
    queryKey: kitchenKeys.queue(restaurantId!),
    queryFn: () => kitchenApi.getQueueStatus(restaurantId!),
    enabled: restaurantId != null,
    staleTime: order_polling_stale_time(),
    refetchInterval: ORDER_POLLING_INTERVAL_MS,
  });
}

/** staleTime = just below the poll interval so each poll actually re-fetches */
function order_polling_stale_time(): number {
  return ORDER_POLLING_INTERVAL_MS - 1000;
}

// ── Mutations ────────────────────────────────────────────────────────────────

/**
 * Transition an order from RECEIVED → PREPARING.
 * Automatically invalidates the queue cache after success.
 */
export function useStartPreparing() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ orderId, station }: { orderId: number; station?: string }) =>
      kitchenApi.startPreparing(orderId, station),
    onSuccess: (_data, { orderId }) => {
      // Invalidate all kitchen queue queries so every open dashboard refreshes
      void queryClient.invalidateQueries({ queryKey: kitchenKeys.all });
      void queryClient.invalidateQueries({ queryKey: kitchenKeys.order(orderId) });
    },
  });
}

/**
 * Transition an order from PREPARING → READY.
 * Automatically invalidates the queue cache after success.
 */
export function useMarkReady() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ orderId }: { orderId: number }) => kitchenApi.markReady(orderId),
    onSuccess: (_data, { orderId }) => {
      void queryClient.invalidateQueries({ queryKey: kitchenKeys.all });
      void queryClient.invalidateQueries({ queryKey: kitchenKeys.order(orderId) });
    },
  });
}

/**
 * Transition an order from READY → PICKED_UP.
 * Automatically invalidates the queue cache after success.
 */
export function useMarkPickedUp() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ orderId }: { orderId: number }) => kitchenApi.markPickedUp(orderId),
    onSuccess: (_data, { orderId }) => {
      void queryClient.invalidateQueries({ queryKey: kitchenKeys.all });
      void queryClient.invalidateQueries({ queryKey: kitchenKeys.order(orderId) });
    },
  });
}

// ── WebSocket ─────────────────────────────────────────────────────────────────

/**
 * Opens a STOMP/SockJS WebSocket subscription to the kitchen topic for a
 * restaurant. When the server pushes a QueueStatusDTO update, the React Query
 * cache is updated immediately — no additional HTTP round-trip required.
 *
 * Returns `{ connected }` — a boolean indicating current WebSocket health.
 * The connection auto-reconnects with exponential backoff on failure.
 */
export function useKitchenWebSocket(restaurantId: number | undefined): {
  connected: boolean;
} {
  const queryClient = useQueryClient();
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (restaurantId == null) return;

    const ws = createKitchenWebSocket({
      restaurantId,
      onMessage: (data: QueueStatusDTO) => {
        queryClient.setQueryData(kitchenKeys.queue(restaurantId), data);
      },
      onConnect: () => setConnected(true),
      onDisconnect: () => setConnected(false),
      onError: (err) => {
        console.error("[KDS WebSocket]", err.message);
        setConnected(false);
      },
    });

    ws.connect();

    return () => {
      ws.disconnect();
    };
  }, [restaurantId, queryClient]);

  return { connected };
}
