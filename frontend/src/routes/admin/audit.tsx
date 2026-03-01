import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

export const Route = createFileRoute("/admin/audit")({
  component: AdminAuditRoute,
});
function AdminAuditRoute() {
  return <ProtectedRoute><div className="py-24 text-center text-muted-foreground">Audit Feed — Sequence 8</div></ProtectedRoute>;
}
