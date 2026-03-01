import { createRootRouteWithContext } from "@tanstack/react-router";
import { TanStackRouterDevtools } from "@tanstack/router-devtools";
import type { AuthContextProps } from "react-oidc-context";
import { ErrorBoundary } from "@/components/feedback/ErrorBoundary";
import { Shell } from "@/components/layout/Shell";

export interface RouterContext {
  auth: AuthContextProps;
}

export const Route = createRootRouteWithContext<RouterContext>()({
  component: RootComponent,
});

function RootComponent() {
  return (
    <ErrorBoundary>
      <Shell />
      {import.meta.env.DEV && <TanStackRouterDevtools position="bottom-right" />}
    </ErrorBoundary>
  );
}
