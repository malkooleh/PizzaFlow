import type { ReactNode } from "react";
import { useAuth } from "react-oidc-context";
import { hasRole } from "@/lib/auth";
import type { UserRole } from "@/types/enums";

interface RoleGuardProps {
  /** Allowed roles — user must have at least one. */
  roles: UserRole[];
  children: ReactNode;
  /** Optional fallback element when access is denied. Defaults to null. */
  fallback?: ReactNode;
}

/**
 * Component-level role guard. Renders `children` only if the authenticated
 * user has at least one of the specified roles. Use `<ProtectedRoute>` for
 * route-level protection (handled in TanStack Router's `beforeLoad`).
 */
export function RoleGuard({ roles, children, fallback = null }: RoleGuardProps) {
  const auth = useAuth();

  if (!auth.isAuthenticated || !hasRole(auth, roles)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}
