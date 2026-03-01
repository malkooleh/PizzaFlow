import type { ReactNode } from "react";
import { Link } from "@tanstack/react-router";
import { Card, CardContent } from "@/components/ui/card";
import { StatusBadge } from "@/components/common/StatusBadge";
import { formatCurrency, formatRelativeTime } from "@/lib/format";
import { OrderStatus, OrderType } from "@/types/enums";
import type { OrderResponse } from "@/types/models";
import { Truck, PackageCheck, UtensilsCrossed, CalendarClock, ChevronRight } from "lucide-react";

const ORDER_TYPE_ICON: Record<OrderType, ReactNode> = {
  [OrderType.DELIVERY]: <Truck className="h-4 w-4" />,
  [OrderType.PICKUP]: <PackageCheck className="h-4 w-4" />,
  [OrderType.DINE_IN]: <UtensilsCrossed className="h-4 w-4" />,
  [OrderType.SCHEDULED]: <CalendarClock className="h-4 w-4" />,
};

const ORDER_TYPE_LABEL: Record<OrderType, string> = {
  [OrderType.DELIVERY]: "Delivery",
  [OrderType.PICKUP]: "Pickup",
  [OrderType.DINE_IN]: "Dine-in",
  [OrderType.SCHEDULED]: "Scheduled",
};

const ACTIVE_STATUSES = new Set([
  OrderStatus.PENDING,
  OrderStatus.CONFIRMED,
  OrderStatus.PREPARING,
  OrderStatus.READY,
  OrderStatus.PICKED_UP,
  OrderStatus.OUT_FOR_DELIVERY,
]);

interface OrderCardProps {
  order: OrderResponse;
}

export function OrderCard({ order }: OrderCardProps) {
  const isActive = ACTIVE_STATUSES.has(order.status);
  const itemSummary =
    order.items.length === 1
      ? order.items[0].menuItemName
      : `${order.items[0].menuItemName} + ${order.items.length - 1} more`;

  return (
    <Link
      to="/orders/$orderId"
      params={{ orderId: String(order.id) }}
      className="block focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded-lg"
    >
      <Card className="transition-shadow hover:shadow-md">
        <CardContent className="p-4">
          <div className="flex items-start justify-between gap-3">
            {/* Left: order info */}
            <div className="min-w-0 space-y-1.5">
              <div className="flex items-center gap-2 flex-wrap">
                <span className="font-semibold text-sm">{order.orderNumber}</span>
                <StatusBadge status={order.status} />
                {isActive && (
                  <span className="inline-flex h-2 w-2 rounded-full bg-primary animate-pulse" />
                )}
              </div>

              <p className="text-sm text-muted-foreground truncate">{itemSummary}</p>

              <div className="flex items-center gap-3 text-xs text-muted-foreground">
                <span className="flex items-center gap-1">
                  {ORDER_TYPE_ICON[order.orderType]}
                  {ORDER_TYPE_LABEL[order.orderType]}
                </span>
                <span>·</span>
                <span>{order.items.length} {order.items.length === 1 ? "item" : "items"}</span>
                <span>·</span>
                <span>{formatRelativeTime(order.createdAt)}</span>
              </div>
            </div>

            {/* Right: total + chevron */}
            <div className="flex items-center gap-2 shrink-0">
              <span className="font-semibold text-sm">
                {formatCurrency(order.totalAmount)}
              </span>
              <ChevronRight className="h-4 w-4 text-muted-foreground" />
            </div>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}
