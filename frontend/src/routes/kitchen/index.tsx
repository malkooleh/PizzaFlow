import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { ChefHat } from "lucide-react";

export const Route = createFileRoute("/kitchen/")({
  component: KitchenRoute,
});

function KitchenRoute() {
  return (
    <ProtectedRoute>
      <div className="flex flex-col items-center justify-center py-24 gap-3 text-center">
        <ChefHat className="h-12 w-12 text-muted-foreground/40" />
        <h1 className="text-xl font-semibold">Kitchen Display System</h1>
        <p className="text-muted-foreground text-sm">Coming in Sequence 6 — KDS</p>
      </div>
    </ProtectedRoute>
  );
}
