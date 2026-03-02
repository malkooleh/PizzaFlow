import { useState } from "react";
import { Wifi, WifiOff, Loader2, MapPin } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useGoOffline, useGoOnline } from "@/hooks/use-deliveries";
import type { CourierResponse } from "@/types/models";
import { CourierStatus } from "@/types/enums";

interface CourierToggleProps {
  courier: CourierResponse;
}

const STATUS_LABEL: Record<CourierStatus, string> = {
  [CourierStatus.OFFLINE]: "Offline",
  [CourierStatus.AVAILABLE]: "Available",
  [CourierStatus.ON_DELIVERY]: "On Delivery",
  [CourierStatus.BREAK]: "On Break",
};

export function CourierToggle({ courier }: CourierToggleProps) {
  const [locationError, setLocationError] = useState<string | null>(null);
  const goOnline = useGoOnline();
  const goOffline = useGoOffline();

  const isOnline = courier.isOnline;
  const isPending = goOnline.isPending || goOffline.isPending;

  function handleToggle() {
    if (isOnline) {
      goOffline.mutate(courier.id);
      return;
    }

    if (!navigator.geolocation) {
      setLocationError("Geolocation is not supported by this browser.");
      return;
    }

    setLocationError(null);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        goOnline.mutate({
          courierId: courier.id,
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude,
        });
      },
      () => {
        setLocationError(
          "Location permission denied. Please allow location access to go online.",
        );
      },
    );
  }

  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="text-base">Courier Status</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="flex items-center justify-between">
          <div>
            <p className="font-medium">{courier.name}</p>
            <p className="text-sm text-muted-foreground">{courier.vehicleType}</p>
          </div>
          <Badge variant={isOnline ? "success" : "secondary"}>
            {STATUS_LABEL[courier.status]}
          </Badge>
        </div>

        <Button
          className="w-full"
          variant={isOnline ? "outline" : "default"}
          disabled={isPending || courier.status === CourierStatus.ON_DELIVERY}
          onClick={handleToggle}
        >
          {isPending ? (
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
          ) : isOnline ? (
            <WifiOff className="mr-2 h-4 w-4" />
          ) : (
            <Wifi className="mr-2 h-4 w-4" />
          )}
          {isOnline ? "Go Offline" : "Go Online"}
        </Button>

        {courier.status === CourierStatus.ON_DELIVERY && (
          <p className="text-xs text-muted-foreground text-center">
            Complete your current delivery first.
          </p>
        )}

        {locationError && (
          <div className="flex items-start gap-2 rounded-md bg-destructive/10 p-2 text-xs text-destructive">
            <MapPin className="mt-0.5 h-3 w-3 shrink-0" />
            {locationError}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
