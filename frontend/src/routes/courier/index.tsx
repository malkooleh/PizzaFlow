import { useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import { Lock } from "lucide-react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { hasRole } from "@/lib/auth";
import { UserRole } from "@/types/enums";
import { CourierToggle } from "@/features/delivery/components/CourierToggle";
import { DeliveryList } from "@/features/delivery/components/DeliveryList";
import { DeliveryDetail } from "@/features/delivery/components/DeliveryDetail";
import { DeliveryStatusFlow } from "@/features/delivery/components/DeliveryStatusFlow";
import { DeliveryMap } from "@/features/delivery/components/DeliveryMap";
import { LocationTracker } from "@/features/delivery/components/LocationTracker";
import { useCourierDeliveries, useCourierProfile } from "@/hooks/use-deliveries";
import { Skeleton } from "@/components/ui/skeleton";
import type { DeliveryResponse } from "@/types/models";

export const Route = createFileRoute("/courier/")({
  component: CourierRoute,
});

const ALLOWED_ROLES = [UserRole.COURIER, UserRole.SYSTEM_ADMIN];

function CourierRoute() {
  const auth = useAuth();
  return (
    <ProtectedRoute>
      {!hasRole(auth, ALLOWED_ROLES) ? (
        <div className="flex flex-col items-center justify-center py-24 gap-3 text-center px-4">
          <Lock className="h-12 w-12 text-muted-foreground/40" />
          <h1 className="text-xl font-semibold">Access Restricted</h1>
          <p className="text-muted-foreground text-sm">
            The Courier Dashboard requires the Courier role.
          </p>
        </div>
      ) : (
        <CourierDashboard />
      )}
    </ProtectedRoute>
  );
}

function CourierDashboard() {
  const auth = useAuth();
  const userId = auth.user?.profile.sub;

  const { data: courier, isLoading: courierLoading } = useCourierProfile(userId);
  const { data: deliveries = [], isLoading: deliveriesLoading } =
    useCourierDeliveries(courier?.id);

  const [selected, setSelected] = useState<DeliveryResponse | null>(null);

  if (courierLoading) {
    return (
      <div className="p-4 space-y-3">
        <Skeleton className="h-28 w-full" />
        <Skeleton className="h-16 w-full" />
        <Skeleton className="h-16 w-full" />
      </div>
    );
  }

  if (!courier) {
    return (
      <div className="flex items-center justify-center py-24 text-center">
        <div>
          <p className="font-semibold">Courier profile not found</p>
          <p className="text-sm text-muted-foreground">
            Your account is not linked to a courier profile.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-4 space-y-4 max-w-lg mx-auto">
      {/* GPS background tracker (online only) */}
      {courier.isOnline && <LocationTracker courierId={courier.id} />}

      {/* Online/Offline toggle */}
      <CourierToggle courier={courier} />

      {/* Delivery list */}
      <DeliveryList
        deliveries={deliveries}
        selectedId={selected?.id}
        onSelect={setSelected}
      />

      {/* Delivery detail + actions */}
      {selected && (
        <div className="space-y-3">
          <DeliveryMap
            delivery={selected}
            courierLocation={courier.currentLocation}
          />
          <DeliveryDetail delivery={selected} />
          <DeliveryStatusFlow delivery={selected} />
        </div>
      )}
    </div>
  );
}
