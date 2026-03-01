import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { ShoppingBag } from "lucide-react";

export const Route = createFileRoute("/orders/")({
  component: OrdersRoute,
});

function OrdersRoute() {
  return (
    <ProtectedRoute>
      <div className="flex flex-col items-center justify-center py-24 gap-3 text-center">
        <ShoppingBag className="h-12 w-12 text-muted-foreground/40" />
        <h1 className="text-xl font-semibold">My Orders</h1>
        <p className="text-muted-foreground text-sm">Coming in Sequence 4 — Order Tracking &amp; History</p>
      </div>
    </ProtectedRoute>
  );
}
