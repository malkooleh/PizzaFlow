import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

export const Route = createFileRoute("/admin/analytics")({
  component: AdminAnalyticsRoute,
});
function AdminAnalyticsRoute() {
  return <ProtectedRoute><div className="py-24 text-center text-muted-foreground">Platform Analytics — Sequence 8</div></ProtectedRoute>;
}
