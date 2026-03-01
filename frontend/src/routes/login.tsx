import { createFileRoute } from "@tanstack/react-router";
import { useEffect } from "react";
import { useAuth } from "react-oidc-context";
import { LoadingSpinner } from "@/components/feedback/LoadingSpinner";

export const Route = createFileRoute("/login")({
  component: LoginPage,
});

/**
 * Immediately triggers the OIDC redirect to Keycloak.
 * Users should never visually linger on this page — it's a redirect trigger.
 */
function LoginPage() {
  const auth = useAuth();

  useEffect(() => {
    if (!auth.isAuthenticated && !auth.isLoading) {
      void auth.signinRedirect();
    }
  }, [auth]);

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4">
      <LoadingSpinner size="lg" label="Redirecting to sign in…" />
      <p className="text-sm text-muted-foreground">Redirecting to sign in…</p>
    </div>
  );
}
