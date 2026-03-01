import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

export const Route = createFileRoute("/manager/bookings")({
  component: ManagerBookingsRoute,
});
function ManagerBookingsRoute() {
  return <ProtectedRoute><div className="py-24 text-center text-muted-foreground">Manager Bookings — Sequence 7</div></ProtectedRoute>;
}
