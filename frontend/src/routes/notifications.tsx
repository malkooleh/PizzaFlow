import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Bell } from "lucide-react";
import { NotificationPreferences } from "@/features/notifications/components/NotificationPreferences";

export const Route = createFileRoute("/notifications")({
  component: NotificationsRoute,
});

function NotificationsRoute() {
  return (
    <ProtectedRoute>
      <div className="p-6 space-y-6">
        <div className="flex items-center gap-3">
          <Bell className="h-6 w-6 text-primary" />
          <div>
            <h1 className="text-2xl font-bold">Notifications</h1>
            <p className="text-muted-foreground text-sm">Manage how and when you receive notifications.</p>
          </div>
        </div>
        <NotificationPreferences />
      </div>
    </ProtectedRoute>
  );
}
