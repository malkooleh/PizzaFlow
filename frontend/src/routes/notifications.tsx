import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Bell } from "lucide-react";

export const Route = createFileRoute("/notifications")({
  component: NotificationsRoute,
});

function NotificationsRoute() {
  return (
    <ProtectedRoute>
      <div className="flex flex-col items-center justify-center py-24 gap-3 text-center">
        <Bell className="h-12 w-12 text-muted-foreground/40" />
        <h1 className="text-xl font-semibold">Notifications</h1>
        <p className="text-muted-foreground text-sm">Coming in Sequence 7</p>
      </div>
    </ProtectedRoute>
  );
}
