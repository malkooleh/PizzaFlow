import { Client, type StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { KITCHEN_WS_URL } from "@/lib/constants";
import type { QueueStatusDTO } from "@/types/models";

// ── Types ─────────────────────────────────────────────────────────────────────

export interface KitchenWebSocketOptions {
  restaurantId: number;
  onMessage: (data: QueueStatusDTO) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: Error) => void;
}

export interface KitchenWebSocketHandle {
  connect: () => void;
  disconnect: () => void;
  isConnected: () => boolean;
}

// ── Exponential backoff helper ────────────────────────────────────────────────

const BASE_DELAY_MS = 2_000;
const MAX_DELAY_MS = 30_000;

function calcBackoffDelay(attempt: number): number {
  return Math.min(BASE_DELAY_MS * Math.pow(2, attempt), MAX_DELAY_MS);
}

// ── Factory ───────────────────────────────────────────────────────────────────

/**
 * Creates a STOMP/SockJS WebSocket connection to the kitchen-service.
 *
 * Subscribes to `/topic/kitchen/{restaurantId}` for real-time QueueStatusDTO updates.
 * Auto-reconnects with true exponential backoff:
 *   attempt 0 → 2 s, attempt 1 → 4 s, attempt 2 → 8 s … capped at 30 s.
 *
 * Usage:
 *   const ws = createKitchenWebSocket({ restaurantId: 1, onMessage: ... });
 *   ws.connect();    // in useEffect on mount
 *   ws.disconnect(); // in useEffect cleanup
 */
export function createKitchenWebSocket(
  options: KitchenWebSocketOptions
): KitchenWebSocketHandle {
  const { restaurantId, onMessage, onConnect, onDisconnect, onError } = options;

  let subscription: StompSubscription | null = null;
  let reconnectAttempt = 0;
  let stopped = false;
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null;

  function scheduleReconnect() {
    if (stopped) return;
    const delay = calcBackoffDelay(reconnectAttempt);
    reconnectAttempt++;
    reconnectTimer = setTimeout(() => {
      if (!stopped) client.activate();
    }, delay);
  }

  const client = new Client({
    // Use SockJS for browser-compatible fallback transport
    webSocketFactory: () => new SockJS(KITCHEN_WS_URL),

    // Disable the built-in fixed-delay reconnect — we manage it ourselves
    reconnectDelay: 0,

    onConnect: () => {
      // Successful connection — reset backoff counter
      reconnectAttempt = 0;
      subscription = client.subscribe(
        `/topic/kitchen/${restaurantId}`,
        (message) => {
          try {
            const data = JSON.parse(message.body) as QueueStatusDTO;
            onMessage(data);
          } catch (err) {
            onError?.(err instanceof Error ? err : new Error(String(err)));
          }
        }
      );
      onConnect?.();
    },

    onDisconnect: () => {
      onDisconnect?.();
      scheduleReconnect();
    },

    onStompError: (frame) => {
      onError?.(new Error(frame.headers["message"] ?? "STOMP error"));
      scheduleReconnect();
    },

    onWebSocketError: () => {
      onError?.(new Error("WebSocket connection error"));
      scheduleReconnect();
    },
  });

  return {
    connect: () => {
      stopped = false;
      reconnectAttempt = 0;
      client.activate();
    },
    disconnect: () => {
      stopped = true;
      if (reconnectTimer !== null) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
      }
      subscription?.unsubscribe();
      subscription = null;
      void client.deactivate();
    },
    isConnected: () => client.connected,
  };
}
