import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { LayoutDashboard } from "lucide-react";

export const Route = createFileRoute("/manager/")({
  component: ManagerRoute,
});

function ManagerRoute() {
  return (
    <ProtectedRoute>
      <div className="flex flex-col items-center justify-center py-24 gap-3 text-center">
        <LayoutDashboard className="h-12 w-12 text-muted-foreground/40" />
        <h1 className="text-xl font-semibold">Manager Dashboard</h1>
        <p className="text-muted-foreground text-sm">Coming in Sequence 7 — Manager Operations</p>
      </div>
    </ProtectedRoute>
  );
}
