import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

export const Route = createFileRoute("/admin/services")({
  component: AdminServicesRoute,
});
function AdminServicesRoute() {
  return <ProtectedRoute><div className="py-24 text-center text-muted-foreground">Service Health — Sequence 8</div></ProtectedRoute>;
}
