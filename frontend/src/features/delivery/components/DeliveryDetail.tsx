import { MapPin, Clock, Hash, Navigation } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { StatusBadge } from "@/components/common/StatusBadge";
import { formatDateTime, formatRelativeTime } from "@/lib/format";
import type { DeliveryResponse } from "@/types/models";

interface DeliveryDetailProps {
  delivery: DeliveryResponse;
}

function InfoRow({
  icon: Icon,
  label,
  value,
}: {
  icon: React.ElementType;
  label: string;
  value: string | undefined | null;
}) {
  if (!value) return null;
  return (
    <div className="flex items-start gap-2 text-sm">
      <Icon className="mt-0.5 h-4 w-4 shrink-0 text-muted-foreground" />
      <div>
        <span className="text-muted-foreground">{label}: </span>
        <span>{value}</span>
      </div>
    </div>
  );
}

export function DeliveryDetail({ delivery }: DeliveryDetailProps) {
  return (
    <Card>
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-base">Delivery Details</CardTitle>
          <StatusBadge status={delivery.status} />
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Order reference */}
        <div className="space-y-1">
          <InfoRow icon={Hash} label="Order" value={delivery.orderNumber} />
          <InfoRow icon={Hash} label="Delivery #" value={delivery.deliveryNumber} />
        </div>

        <Separator />

        {/* Addresses */}
        <div className="space-y-2">
          <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Route
          </p>
          <div className="flex items-start gap-2 text-sm">
            <Navigation className="mt-0.5 h-4 w-4 shrink-0 text-blue-500" />
            <div>
              <p className="text-xs text-muted-foreground">Pick up from</p>
              <p>{delivery.pickupAddress}</p>
            </div>
          </div>
          <div className="flex items-start gap-2 text-sm">
            <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-red-500" />
            <div>
              <p className="text-xs text-muted-foreground">Deliver to</p>
              <p>{delivery.deliveryAddress}</p>
            </div>
          </div>
          {delivery.distanceKm != null && (
            <p className="pl-6 text-xs text-muted-foreground">
              ~{delivery.distanceKm.toFixed(1)} km
            </p>
          )}
        </div>

        <Separator />

        {/* Timing */}
        <div className="space-y-1">
          <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            Timing
          </p>
          <InfoRow
            icon={Clock}
            label="ETA"
            value={
              delivery.estimatedDeliveryTime
                ? formatRelativeTime(delivery.estimatedDeliveryTime)
                : undefined
            }
          />
          <InfoRow
            icon={Clock}
            label="Assigned"
            value={formatDateTime(delivery.createdAt)}
          />
          {delivery.actualDeliveryTime && (
            <InfoRow
              icon={Clock}
              label="Completed"
              value={formatDateTime(delivery.actualDeliveryTime)}
            />
          )}
        </div>
      </CardContent>
    </Card>
  );
}
