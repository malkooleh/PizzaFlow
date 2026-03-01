import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Package } from "lucide-react";

export const Route = createFileRoute("/orders/$orderId")({
  component: OrderDetailRoute,
});

function OrderDetailRoute() {
  const { orderId } = Route.useParams();
  return (
    <ProtectedRoute>
      <div className="flex flex-col items-center justify-center py-24 gap-3 text-center">
        <Package className="h-12 w-12 text-muted-foreground/40" />
        <h1 className="text-xl font-semibold">Order #{orderId}</h1>
        <p className="text-muted-foreground text-sm">Order detail &amp; tracking — coming in Sequence 4</p>
      </div>
    </ProtectedRoute>
  );
}
