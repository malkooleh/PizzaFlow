import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { ShieldCheck } from "lucide-react";

export const Route = createFileRoute("/admin/")({
  component: AdminRoute,
});

function AdminRoute() {
  return (
    <ProtectedRoute>
      <div className="flex flex-col items-center justify-center py-24 gap-3 text-center">
        <ShieldCheck className="h-12 w-12 text-muted-foreground/40" />
        <h1 className="text-xl font-semibold">Admin Overview</h1>
        <p className="text-muted-foreground text-sm">Coming in Sequence 8 — Admin Panel</p>
      </div>
    </ProtectedRoute>
  );
}
