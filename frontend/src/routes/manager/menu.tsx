import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

export const Route = createFileRoute("/manager/menu")({
  component: ManagerMenuRoute,
});
function ManagerMenuRoute() {
  return <ProtectedRoute><div className="py-24 text-center text-muted-foreground">Menu Management — Sequence 7</div></ProtectedRoute>;
}
