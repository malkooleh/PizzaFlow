import { useAuth } from "react-oidc-context";
import { useNavigate } from "@tanstack/react-router";
import { useEffect } from "react";
import { LoadingSpinner } from "@/components/feedback/LoadingSpinner";

/**
 * Wraps a route subtree and redirects unauthenticated users to /login.
 * Role-specific protection is handled at the route level via beforeLoad.
 */
export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const auth = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!auth.isLoading && !auth.isAuthenticated) {
      void navigate({ to: "/login" });
    }
  }, [auth.isAuthenticated, auth.isLoading, navigate]);

  if (auth.isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!auth.isAuthenticated) return null;

  return <>{children}</>;
}
