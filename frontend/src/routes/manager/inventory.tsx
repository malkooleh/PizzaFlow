import { createFileRoute } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import { Lock } from "lucide-react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { hasRole } from "@/lib/auth";
import { UserRole } from "@/types/enums";
import { InventoryOverview } from "@/features/manager/components/InventoryOverview";
import { useUiStore } from "@/stores/ui.store";

export const Route = createFileRoute("/manager/inventory")({
  component: ManagerInventoryRoute,
});

const ALLOWED_ROLES = [UserRole.RESTAURANT_MANAGER, UserRole.SYSTEM_ADMIN];

function ManagerInventoryRoute() {
  const auth = useAuth();
  const restaurantId = useUiStore((s) => s.selectedRestaurantId);
  return (
    <ProtectedRoute>
      {!hasRole(auth, ALLOWED_ROLES) ? (
        <div className="flex flex-col items-center justify-center py-24 gap-3 text-center px-4">
          <Lock className="h-12 w-12 text-muted-foreground/40" />
          <h1 className="text-xl font-semibold">Access Restricted</h1>
          <p className="text-muted-foreground text-sm">
            This page requires the Restaurant Manager role.
          </p>
        </div>
      ) : (
        <div className="p-4 space-y-4">
          <h1 className="text-xl font-semibold">Inventory Overview</h1>
          {restaurantId ? (
            <InventoryOverview restaurantId={restaurantId} />
          ) : (
            <p className="text-muted-foreground text-sm">Select a restaurant first.</p>
          )}
        </div>
      )}
    </ProtectedRoute>
  );
}
