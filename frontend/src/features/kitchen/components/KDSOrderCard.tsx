import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { KitchenOrderStatus } from "@/types/enums";
import type { KitchenOrderDTO } from "@/types/models";
import { TimerBadge } from "./TimerBadge";
import { OrderPriorityIndicator } from "./OrderPriorityIndicator";

const ORDER_TYPE_LABELS: Record<string, string> = {
  DELIVERY: "Delivery",
  PICKUP: "Pickup",
  DINE_IN: "Dine-In",
  SCHEDULED: "Scheduled",
};

/** Map each kitchen status to the label of the available action. */
const ACTION_LABELS: Partial<Record<KitchenOrderStatus, string>> = {
  [KitchenOrderStatus.RECEIVED]: "Start Preparing",
  [KitchenOrderStatus.PREPARING]: "Mark Ready",
  [KitchenOrderStatus.READY]: "Mark Picked Up",
};

interface KDSOrderCardProps {
  order: KitchenOrderDTO;
  onStartPreparing?: (orderId: number, station?: string) => void;
  onMarkReady?: (orderId: number) => void;
  onMarkPickedUp?: (orderId: number) => void;
  isLoading?: boolean;
}

/**
 * Order card for the Kitchen Display System.
 * Shows order meta-data, item list, elapsed timer, and a single CTA button
 * that advances the order through RECEIVED → PREPARING → READY → PICKED_UP.
 */
export function KDSOrderCard({
  order,
  onStartPreparing,
  onMarkReady,
  onMarkPickedUp,
  isLoading = false,
}: KDSOrderCardProps) {
  const actionLabel = ACTION_LABELS[order.status];

  const handleAction = () => {
    switch (order.status) {
      case KitchenOrderStatus.RECEIVED:
        onStartPreparing?.(order.orderId);
        break;
      case KitchenOrderStatus.PREPARING:
        onMarkReady?.(order.orderId);
        break;
      case KitchenOrderStatus.READY:
        onMarkPickedUp?.(order.orderId);
        break;
    }
  };

  return (
    <Card className="shadow-sm" data-testid="kds-order-card">
      <CardHeader className="pb-2 pt-3 px-3 flex flex-row items-start justify-between gap-2">
        <div className="min-w-0 space-y-1">
          <p className="font-bold text-sm">{order.orderNumber}</p>
          <div className="flex items-center gap-1 flex-wrap">
            <Badge variant="secondary" className="text-xs">
              {ORDER_TYPE_LABELS[order.orderType] ?? order.orderType}
            </Badge>
            <OrderPriorityIndicator priority={order.priority} />
          </div>
        </div>
        <TimerBadge receivedAt={order.receivedAt} className="shrink-0" />
      </CardHeader>

      <CardContent className="px-3 pb-3 space-y-2">
        {/* Queue position */}
        {order.queuePosition > 0 && (
          <p className="text-xs text-muted-foreground">
            Queue #{order.queuePosition}
          </p>
        )}

        {/* Item list */}
        <ul className="space-y-0.5" aria-label="Order items">
          {order.items.map((item, index) => (
            <li key={index} className="text-xs flex items-start gap-1.5">
              <span className="font-semibold shrink-0 tabular-nums">
                {item.quantity}×
              </span>
              <span className="flex-1">{item.menuItemName}</span>
              {item.specialInstructions && (
                <span className="text-orange-600 italic text-xs">
                  ({item.specialInstructions})
                </span>
              )}
            </li>
          ))}
        </ul>

        {/* Order-level special instructions */}
        {order.specialInstructions && (
          <p className="text-xs text-orange-700 bg-orange-50 border border-orange-200 rounded px-2 py-1">
            ⚠ {order.specialInstructions}
          </p>
        )}

        {/* Assigned station */}
        {order.assignedStation && (
          <p className="text-xs text-muted-foreground">
            Station: <span className="font-medium">{order.assignedStation}</span>
          </p>
        )}

        {/* Estimated prep time */}
        {order.estimatedPrepTimeMinutes != null && (
          <p className="text-xs text-muted-foreground">
            Est. {order.estimatedPrepTimeMinutes} min
          </p>
        )}

        {/* Action button */}
        {actionLabel && (
          <Button
            size="sm"
            className="w-full mt-1"
            onClick={handleAction}
            disabled={isLoading}
          >
            {actionLabel}
          </Button>
        )}
      </CardContent>
    </Card>
  );
}
