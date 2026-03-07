import type { RefObject } from "react";
import { KitchenOrderStatus } from "@/types/enums";
import type { KitchenOrderDTO } from "@/types/models";
import { KDSOrderCard } from "./KDSOrderCard";

// ── Column configuration ──────────────────────────────────────────────────────

type ActiveStatus =
  | KitchenOrderStatus.RECEIVED
  | KitchenOrderStatus.PREPARING
  | KitchenOrderStatus.READY;

const COLUMN_CONFIG: Record<
  ActiveStatus,
  { label: string; headerClass: string; emptyText: string }
> = {
  [KitchenOrderStatus.RECEIVED]: {
    label: "Received",
    headerClass: "bg-slate-100 text-slate-700",
    emptyText: "No new orders",
  },
  [KitchenOrderStatus.PREPARING]: {
    label: "Preparing",
    headerClass: "bg-blue-100 text-blue-700",
    emptyText: "Nothing cooking yet",
  },
  [KitchenOrderStatus.READY]: {
    label: "Ready",
    headerClass: "bg-green-100 text-green-700",
    emptyText: "No orders ready yet",
  },
};

// ── Component ─────────────────────────────────────────────────────────────────

interface StationColumnProps {
  status: ActiveStatus;
  orders: KitchenOrderDTO[];
  onStartPreparing?: (orderId: number, station?: string) => void;
  onMarkReady?: (orderId: number) => void;
  onMarkPickedUp?: (orderId: number) => void;
  /** orderId that is currently being transitioned — disables that card's button. */
  loadingOrderId?: number | null;
  /** Attach to the scrollable card list to allow programmatic scrolling. */
  scrollContainerRef?: RefObject<HTMLDivElement | null>;
}

/**
 * A single Kanban column showing all orders in a given kitchen status.
 * Vertically scrollable; shows an empty-state message when no orders exist.
 */
export function StationColumn({
  status,
  orders,
  onStartPreparing,
  onMarkReady,
  onMarkPickedUp,
  loadingOrderId,
  scrollContainerRef,
}: Readonly<StationColumnProps>) {
  const config = COLUMN_CONFIG[status];

  return (
    <div
      className="flex flex-col min-h-0 rounded-lg overflow-hidden border"
      data-testid={`station-column-${status.toLowerCase()}`}
    >
      {/* Column header */}
      <div
        className={`flex items-center justify-between px-3 py-2 ${config.headerClass}`}
      >
        <h2 className="font-semibold text-sm">{config.label}</h2>
        <span className="inline-flex items-center justify-center h-5 min-w-5 rounded-full bg-white/70 text-xs font-bold px-1">
          {orders.length}
        </span>
      </div>

      {/* Scrollable card list */}
      <div ref={scrollContainerRef} className="flex-1 overflow-y-auto p-2 space-y-2 bg-muted/20 min-h-[200px]">
        {orders.length === 0 ? (
          <p className="text-xs text-muted-foreground text-center py-8">
            {config.emptyText}
          </p>
        ) : (
          orders.map((order) => (
            <KDSOrderCard
              key={order.id}
              order={order}
              onStartPreparing={onStartPreparing}
              onMarkReady={onMarkReady}
              onMarkPickedUp={onMarkPickedUp}
              isLoading={loadingOrderId === order.orderId}
            />
          ))
        )}
      </div>
    </div>
  );
}
