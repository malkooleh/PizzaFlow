import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Truck } from "lucide-react";

export const Route = createFileRoute("/courier/")({
  component: CourierRoute,
});

function CourierRoute() {
  return (
    <ProtectedRoute>
      <div className="flex flex-col items-center justify-center py-24 gap-3 text-center">
        <Truck className="h-12 w-12 text-muted-foreground/40" />
        <h1 className="text-xl font-semibold">Active Deliveries</h1>
        <p className="text-muted-foreground text-sm">Coming in Sequence 7 — Courier Operations</p>
      </div>
    </ProtectedRoute>
  );
}
