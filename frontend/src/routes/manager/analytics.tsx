import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

export const Route = createFileRoute("/manager/analytics")({
  component: ManagerAnalyticsRoute,
});
function ManagerAnalyticsRoute() {
  return <ProtectedRoute><div className="py-24 text-center text-muted-foreground">Analytics — Sequence 7</div></ProtectedRoute>;
}
