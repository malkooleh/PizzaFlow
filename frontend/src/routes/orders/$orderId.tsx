import { createFileRoute, Link } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Button } from "@/components/ui/button";
import { ChevronLeft } from "lucide-react";
import { OrderDetail } from "@/features/orders/components/OrderDetail";

export const Route = createFileRoute("/orders/$orderId")({
  component: OrderDetailRoute,
});

function OrderDetailRoute() {
  const { orderId } = Route.useParams();
  const id = Number(orderId);

  return (
    <ProtectedRoute>
      <div className="container max-w-3xl py-8 space-y-6">
        <Button asChild variant="ghost" size="sm" className="-ml-2">
          <Link to="/orders">
            <ChevronLeft className="mr-1 h-4 w-4" />
            Back to orders
          </Link>
        </Button>
        <OrderDetail orderId={id} />
      </div>
    </ProtectedRoute>
  );
}
