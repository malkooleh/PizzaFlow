import { createFileRoute } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import { Lock, History, ChefHat } from "lucide-react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { hasRole } from "@/lib/auth";
import { UserRole } from "@/types/enums";

export const Route = createFileRoute("/kitchen/history")({
  component: KitchenHistoryRoute,
});

const ALLOWED_ROLES: UserRole[] = [
  UserRole.KITCHEN_STAFF,
  UserRole.RESTAURANT_MANAGER,
  UserRole.SYSTEM_ADMIN,
];

function KitchenHistoryRoute() {
  const auth = useAuth();

  return (
    <ProtectedRoute>
      {!hasRole(auth, ALLOWED_ROLES) ? (
        <div className="flex flex-col items-center justify-center py-24 gap-3 text-center px-4">
          <Lock className="h-12 w-12 text-muted-foreground/40" />
          <h1 className="text-xl font-semibold">Access Restricted</h1>
          <p className="text-muted-foreground text-sm">
            Queue History requires Kitchen Staff or Manager role.
          </p>
        </div>
      ) : (
        <KitchenHistoryView />
      )}
    </ProtectedRoute>
  );
}

function KitchenHistoryView() {
  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 text-muted-foreground">
          <ChefHat className="h-5 w-5" />
          <History className="h-5 w-5" />
        </div>
        <div>
          <h1 className="text-2xl font-bold">Queue History</h1>
          <p className="text-muted-foreground text-sm">
            Completed and cancelled orders from this kitchen session.
          </p>
        </div>
      </div>

      <div className="flex flex-col items-center justify-center py-16 gap-3 text-center rounded-lg border border-dashed">
        <History className="h-10 w-10 text-muted-foreground/40" />
        <p className="text-muted-foreground text-sm">
          Queue history will display here once orders are completed.
        </p>
      </div>
    </div>
  );
}
