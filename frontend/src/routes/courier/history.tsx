import { createFileRoute } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import { Lock, History, Truck } from "lucide-react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { hasRole } from "@/lib/auth";
import { UserRole } from "@/types/enums";

export const Route = createFileRoute("/courier/history")({
  component: DeliveryHistoryRoute,
});

const ALLOWED_ROLES = [UserRole.COURIER, UserRole.SYSTEM_ADMIN];

function DeliveryHistoryRoute() {
  const auth = useAuth();

  return (
    <ProtectedRoute>
      {!hasRole(auth, ALLOWED_ROLES) ? (
        <div className="flex flex-col items-center justify-center py-24 gap-3 text-center px-4">
          <Lock className="h-12 w-12 text-muted-foreground/40" />
          <h1 className="text-xl font-semibold">Access Restricted</h1>
          <p className="text-muted-foreground text-sm">
            Delivery History requires the Courier role.
          </p>
        </div>
      ) : (
        <DeliveryHistoryView />
      )}
    </ProtectedRoute>
  );
}

function DeliveryHistoryView() {
  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 text-muted-foreground">
          <Truck className="h-5 w-5" />
          <History className="h-5 w-5" />
        </div>
        <div>
          <h1 className="text-2xl font-bold">Delivery History</h1>
          <p className="text-muted-foreground text-sm">
            Your completed and past deliveries.
          </p>
        </div>
      </div>

      <div className="flex flex-col items-center justify-center py-16 gap-3 text-center rounded-lg border border-dashed">
        <History className="h-10 w-10 text-muted-foreground/40" />
        <p className="text-muted-foreground text-sm">
          Completed deliveries will appear here.
        </p>
      </div>
    </div>
  );
}
