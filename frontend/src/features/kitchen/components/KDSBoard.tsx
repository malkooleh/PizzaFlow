import { useEffect, useRef, useState } from "react";
import { AlertCircle, Wifi, WifiOff } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import { KitchenOrderStatus } from "@/types/enums";
import type { KitchenOrderDTO } from "@/types/models";
import { useKdsStore } from "@/stores/kds.store";
import { playNewOrderAlert } from "@/lib/audio";
import {
  useKitchenQueue,
  useStartPreparing,
  useMarkReady,
  useMarkPickedUp,
  useKitchenWebSocket,
} from "@/hooks/use-kitchen";
import { QueueStats } from "./QueueStats";
import { StationColumn } from "./StationColumn";
import { KDSSettings } from "./KDSSettings";

// ── Helpers ───────────────────────────────────────────────────────────────────

function filterByStatus(orders: KitchenOrderDTO[], status: KitchenOrderStatus) {
  return orders.filter((o) => o.status === status);
}

// ── Component ─────────────────────────────────────────────────────────────────

interface KDSBoardProps {
  restaurantId: number;
}

/**
 * Kitchen Display System — Kanban-style real-time order board.
 *
 * Columns: RECEIVED → PREPARING → READY
 *
 * Data flow:
 *  1. Initial load via REST (polling every 5 s as a safety net).
 *  2. Live updates via STOMP/SockJS WebSocket — injected directly into
 *     the React Query cache, so components re-render without a re-fetch.
 */
export function KDSBoard({ restaurantId }: KDSBoardProps) {
  const [loadingOrderId, setLoadingOrderId] = useState<number | null>(null);
  const receivedScrollRef = useRef<HTMLDivElement>(null);
  const prevReceivedCountRef = useRef<number>(0);

  const { audioAlertsEnabled, autoScroll } = useKdsStore();

  const {
    data: queueStatus,
    isLoading,
    isError,
    error,
  } = useKitchenQueue(restaurantId);

  const { connected } = useKitchenWebSocket(restaurantId);

  const startPreparing = useStartPreparing();
  const markReady = useMarkReady();
  const markPickedUp = useMarkPickedUp();

  // ── Action handlers ────────────────────────────────────────────────────────

  const handleStartPreparing = async (orderId: number, station?: string) => {
    setLoadingOrderId(orderId);
    try {
      await startPreparing.mutateAsync({ orderId, station });
    } finally {
      setLoadingOrderId(null);
    }
  };

  const handleMarkReady = async (orderId: number) => {
    setLoadingOrderId(orderId);
    try {
      await markReady.mutateAsync({ orderId });
    } finally {
      setLoadingOrderId(null);
    }
  };

  const handleMarkPickedUp = async (orderId: number) => {
    setLoadingOrderId(orderId);
    try {
      await markPickedUp.mutateAsync({ orderId });
    } finally {
      setLoadingOrderId(null);
    }
  };

  // ── Audio alert + auto-scroll on new received orders ──────────────────────

  const orders = queueStatus?.orders ?? [];
  const received = filterByStatus(orders, KitchenOrderStatus.RECEIVED);
  const preparing = filterByStatus(orders, KitchenOrderStatus.PREPARING);
  const ready = filterByStatus(orders, KitchenOrderStatus.READY);

  useEffect(() => {
    const currentCount = received.length;
    const prevCount = prevReceivedCountRef.current;

    if (currentCount > prevCount) {
      if (audioAlertsEnabled) {
        playNewOrderAlert();
      }
      if (autoScroll && receivedScrollRef.current) {
        receivedScrollRef.current.scrollTop = 0;
      }
    }

    prevReceivedCountRef.current = currentCount;
  }, [received.length, audioAlertsEnabled, autoScroll]);

  // ── Loading skeleton ───────────────────────────────────────────────────────

  if (isLoading) {
    return (
      <div className="p-4 space-y-4" data-testid="kds-loading">
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-14 rounded-lg" />
          ))}
        </div>
        <div className="grid grid-cols-3 gap-4">
          {Array.from({ length: 3 }).map((_, col) => (
            <div key={col} className="space-y-2">
              <Skeleton className="h-9 rounded-t-lg" />
              {Array.from({ length: 2 }).map((_, row) => (
                <Skeleton key={row} className="h-32 rounded-lg" />
              ))}
            </div>
          ))}
        </div>
      </div>
    );
  }

  // ── Error state ────────────────────────────────────────────────────────────

  if (isError) {
    return (
      <Alert variant="destructive" className="m-4" data-testid="kds-error">
        <AlertCircle className="h-4 w-4" />
        <AlertTitle>Failed to load kitchen queue</AlertTitle>
        <AlertDescription>
          {error instanceof Error
            ? error.message
            : "Please check your connection and try again."}
        </AlertDescription>
      </Alert>
    );
  }

  // ── Board ──────────────────────────────────────────────────────────────────

  return (
    <div className="flex flex-col gap-4 h-full p-4" data-testid="kds-board">
      {/* Header */}
      <div className="flex items-center justify-between gap-2">
        <h1 className="text-lg font-bold">Kitchen Display</h1>
        <div className="flex items-center gap-2">
          {/* WebSocket status indicator */}
          <div
            className="flex items-center gap-1.5 text-xs"
            aria-live="polite"
            aria-label={connected ? "WebSocket connected" : "WebSocket reconnecting"}
          >
            {connected ? (
              <>
                <Wifi className="h-3.5 w-3.5 text-green-500" aria-hidden="true" />
                <span className="text-green-600 font-medium">Live</span>
              </>
            ) : (
              <>
                <WifiOff
                  className="h-3.5 w-3.5 text-muted-foreground"
                  aria-hidden="true"
                />
                <span className="text-muted-foreground">Reconnecting…</span>
              </>
            )}
          </div>
          <KDSSettings />
        </div>
      </div>

      {/* Statistics bar */}
      {queueStatus && <QueueStats stats={queueStatus} />}

      {/* Three-column Kanban board */}
      <div className="grid grid-cols-3 gap-4 flex-1 min-h-0">
        <StationColumn
          status={KitchenOrderStatus.RECEIVED}
          orders={received}
          onStartPreparing={handleStartPreparing}
          loadingOrderId={loadingOrderId}
          scrollContainerRef={receivedScrollRef}
        />
        <StationColumn
          status={KitchenOrderStatus.PREPARING}
          orders={preparing}
          onMarkReady={handleMarkReady}
          loadingOrderId={loadingOrderId}
        />
        <StationColumn
          status={KitchenOrderStatus.READY}
          orders={ready}
          onMarkPickedUp={handleMarkPickedUp}
          loadingOrderId={loadingOrderId}
        />
      </div>
    </div>
  );
}
