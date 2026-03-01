import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { CalendarDays } from "lucide-react";

export const Route = createFileRoute("/bookings/")({
  component: BookingsRoute,
});

function BookingsRoute() {
  return (
    <ProtectedRoute>
      <div className="flex flex-col items-center justify-center py-24 gap-3 text-center">
        <CalendarDays className="h-12 w-12 text-muted-foreground/40" />
        <h1 className="text-xl font-semibold">My Bookings</h1>
        <p className="text-muted-foreground text-sm">Coming in Sequence 5 — Table Bookings</p>
      </div>
    </ProtectedRoute>
  );
}
