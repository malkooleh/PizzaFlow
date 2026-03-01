import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

export const Route = createFileRoute("/manager/inventory")({
  component: ManagerInventoryRoute,
});
function ManagerInventoryRoute() {
  return <ProtectedRoute><div className="py-24 text-center text-muted-foreground">Inventory — Sequence 7</div></ProtectedRoute>;
}
