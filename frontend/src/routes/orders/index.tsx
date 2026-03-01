import { createFileRoute, Link } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import { Button } from "@/components/ui/button";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { getCustomerDbId } from "@/lib/auth";
import { OrderList } from "@/features/orders/components/OrderList";

export const Route = createFileRoute("/orders/")({
  component: OrdersRoute,
});

function OrdersRoute() {
  return (
    <ProtectedRoute>
      <OrdersPage />
    </ProtectedRoute>
  );
}

function OrdersPage() {
  const auth = useAuth();
  const customerId = getCustomerDbId(auth.user);

  return (
    <div className="container max-w-2xl py-8 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">My Orders</h1>
        <Button asChild variant="outline" size="sm">
          <Link to="/menu">Order Again</Link>
        </Button>
      </div>
      <OrderList customerId={customerId} />
    </div>
  );
}
