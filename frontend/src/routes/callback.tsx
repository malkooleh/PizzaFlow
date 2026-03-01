import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useEffect } from "react";
import { useAuth } from "react-oidc-context";
import { LoadingSpinner } from "@/components/feedback/LoadingSpinner";
import { getRoles } from "@/lib/auth";
import { UserRole } from "@/types/enums";

export const Route = createFileRoute("/callback")({
  component: CallbackPage,
});

/**
 * OIDC callback handler.
 *
 * react-oidc-context processes the code/state params automatically via
 * `signinCallback()`. This component simply waits for auth to settle,
 * then redirects authenticated users to their role‑appropriate home route.
 */
function CallbackPage() {
  const auth = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (auth.isLoading) return;

    if (auth.error) {
      console.error("[Callback] OIDC error:", auth.error);
      void navigate({ to: "/login" });
      return;
    }

    if (auth.isAuthenticated) {
      const roles = getRoles(auth.user);

      let to: string;
      if (roles.includes(UserRole.SYSTEM_ADMIN)) to = "/admin";
      else if (roles.includes(UserRole.RESTAURANT_MANAGER)) to = "/manager";
      else if (roles.includes(UserRole.KITCHEN_STAFF)) to = "/kitchen";
      else if (roles.includes(UserRole.COURIER)) to = "/courier";
      else to = "/menu";

      void navigate({ to });
    }
  }, [auth.isAuthenticated, auth.isLoading, auth.error, auth.user, navigate]);

  if (auth.error) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4">
        <p className="text-destructive font-medium">Authentication failed.</p>
        <p className="text-sm text-muted-foreground">{auth.error.message}</p>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4">
      <LoadingSpinner size="lg" label="Completing sign in…" />
      <p className="text-sm text-muted-foreground">Completing sign in…</p>
    </div>
  );
}
