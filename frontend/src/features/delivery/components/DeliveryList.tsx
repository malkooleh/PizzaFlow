import { MapPin, Clock, Package } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { StatusBadge } from "@/components/common/StatusBadge";
import { formatRelativeTime } from "@/lib/format";
import type { DeliveryResponse } from "@/types/models";
import { DeliveryStatus } from "@/types/enums";
import { cn } from "@/lib/utils";

interface DeliveryListProps {
  deliveries: DeliveryResponse[];
  selectedId?: string;
  onSelect: (delivery: DeliveryResponse) => void;
}

const ACTIVE_STATUSES = new Set<DeliveryStatus>([
  DeliveryStatus.PENDING,
  DeliveryStatus.ASSIGNED,
  DeliveryStatus.PICKED_UP,
  DeliveryStatus.IN_TRANSIT,
  DeliveryStatus.ARRIVED,
]);

export function DeliveryList({
  deliveries,
  selectedId,
  onSelect,
}: DeliveryListProps) {
  if (deliveries.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
        <Package className="mb-3 h-10 w-10 opacity-40" />
        <p className="font-medium">No active deliveries</p>
        <p className="text-sm">Go online to receive delivery assignments.</p>
      </div>
    );
  }

  const sorted = [...deliveries].sort((a, b) => {
    const aActive = ACTIVE_STATUSES.has(a.status) ? 0 : 1;
    const bActive = ACTIVE_STATUSES.has(b.status) ? 0 : 1;
    return aActive - bActive;
  });

  return (
    <div className="space-y-2">
      {sorted.map((delivery) => (
        <Card
          key={delivery.id}
          className={cn(
            "cursor-pointer transition-colors hover:bg-accent",
            selectedId === delivery.id && "ring-2 ring-primary",
          )}
          onClick={() => onSelect(delivery)}
        >
          <CardContent className="p-3">
            <div className="flex items-start justify-between gap-2">
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium">
                  Order #{delivery.orderNumber}
                </p>
                <div className="mt-1 flex items-center gap-1 text-xs text-muted-foreground">
                  <MapPin className="h-3 w-3 shrink-0" />
                  <span className="truncate">{delivery.deliveryAddress}</span>
                </div>
                {delivery.estimatedDeliveryTime && (
                  <div className="mt-1 flex items-center gap-1 text-xs text-muted-foreground">
                    <Clock className="h-3 w-3 shrink-0" />
                    <span>{formatRelativeTime(delivery.estimatedDeliveryTime)}</span>
                  </div>
                )}
              </div>
              <StatusBadge status={delivery.status} className="text-xs" />
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
