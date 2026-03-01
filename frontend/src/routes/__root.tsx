import { createRootRouteWithContext, Outlet } from "@tanstack/react-router";
import { TanStackRouterDevtools } from "@tanstack/router-devtools";
import type { AuthContextProps } from "react-oidc-context";
import { ErrorBoundary } from "@/components/feedback/ErrorBoundary";

export interface RouterContext {
  auth: AuthContextProps;
}

export const Route = createRootRouteWithContext<RouterContext>()({
  component: RootComponent,
});

function RootComponent() {
  return (
    <ErrorBoundary>
      <Outlet />
      {import.meta.env.DEV && <TanStackRouterDevtools position="bottom-right" />}
    </ErrorBoundary>
  );
}
