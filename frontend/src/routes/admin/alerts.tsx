import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

export const Route = createFileRoute("/admin/alerts")({
  component: AdminAlertsRoute,
});
function AdminAlertsRoute() {
  return <ProtectedRoute><div className="py-24 text-center text-muted-foreground">Alert Center — Sequence 8</div></ProtectedRoute>;
}
