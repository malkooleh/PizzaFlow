import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { useAuth } from "react-oidc-context";
import { ChefHat, Lock } from "lucide-react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { KDSBoard } from "@/features/kitchen/components/KDSBoard";
import { hasRole } from "@/lib/auth";
import { useKdsStore } from "@/stores/kds.store";
import { UserRole } from "@/types/enums";

export const Route = createFileRoute("/kitchen/")({
  component: KitchenRoute,
});

const ALLOWED_ROLES: UserRole[] = [
  UserRole.KITCHEN_STAFF,
  UserRole.RESTAURANT_MANAGER,
  UserRole.SYSTEM_ADMIN,
];

function KitchenRoute() {
  const auth = useAuth();
  const restaurantIdStr = useKdsStore((s) => s.restaurantId);
  const setRestaurantId = useKdsStore((s) => s.setRestaurantId);
  const [inputValue, setInputValue] = useState("");

  return (
    <ProtectedRoute>
      {/* Role guard */}
      {!hasRole(auth, ALLOWED_ROLES) ? (
        <div className="flex flex-col items-center justify-center py-24 gap-3 text-center px-4">
          <Lock className="h-12 w-12 text-muted-foreground/40" />
          <h1 className="text-xl font-semibold">Access Restricted</h1>
          <p className="text-muted-foreground text-sm">
            The Kitchen Display System requires Kitchen Staff or Manager role.
          </p>
        </div>
      ) : restaurantIdStr == null ? (
        /* Restaurant setup prompt */
        <div className="flex flex-col items-center justify-center py-24 gap-6 px-4 max-w-sm mx-auto">
          <ChefHat className="h-12 w-12 text-muted-foreground/40" />
          <div className="text-center">
            <h1 className="text-xl font-semibold">Kitchen Display System</h1>
            <p className="text-muted-foreground text-sm mt-1">
              Enter your restaurant ID to load the order queue.
            </p>
          </div>
          <div className="w-full space-y-2">
            <Label htmlFor="restaurant-id-input">Restaurant ID</Label>
            <Input
              id="restaurant-id-input"
              type="number"
              min={1}
              placeholder="e.g. 1"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && inputValue.trim()) {
                  setRestaurantId(inputValue.trim());
                }
              }}
            />
            <Button
              className="w-full"
              disabled={!inputValue.trim()}
              onClick={() => setRestaurantId(inputValue.trim())}
            >
              Open Kitchen Display
            </Button>
          </div>
        </div>
      ) : (
        /* Full-screen KDS board */
        <div className="h-full">
          <KDSBoard restaurantId={Number(restaurantIdStr)} />
        </div>
      )}
    </ProtectedRoute>
  );
}
