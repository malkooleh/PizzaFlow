import { createFileRoute } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import { Lock } from "lucide-react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { hasRole } from "@/lib/auth";
import { UserRole } from "@/types/enums";
import { AuditFeed } from "@/features/admin/components/AuditFeed";

export const Route = createFileRoute("/admin/audit")({
  component: AdminAuditRoute,
});

const ALLOWED_ROLES = [UserRole.SYSTEM_ADMIN];

function AdminAuditRoute() {
  const auth = useAuth();
  return (
    <ProtectedRoute>
      {!hasRole(auth, ALLOWED_ROLES) ? (
        <div className="flex flex-col items-center justify-center py-24 gap-3 text-center px-4">
          <Lock className="h-12 w-12 text-muted-foreground/40" />
          <h1 className="text-xl font-semibold">Access Restricted</h1>
          <p className="text-muted-foreground text-sm">
            The Admin Panel requires the System Admin role.
          </p>
        </div>
      ) : (
        <div className="p-4 space-y-4">
          <h1 className="text-xl font-semibold">Audit Feed</h1>
          <AuditFeed />
        </div>
      )}
    </ProtectedRoute>
  );
}
