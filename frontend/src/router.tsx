import { createRouter } from "@tanstack/react-router";
import type { AuthContextProps } from "react-oidc-context";
import { routeTree } from "./routeTree.gen";

/**
 * Router context — injected into every route via the root route.
 * Auth context is provided by AuthProvider; TanStack Router injects it
 * through the context mechanism so route loaders/beforeLoad have access.
 */
export interface RouterContext {
  auth: AuthContextProps;
}

export const router = createRouter({
  routeTree,
  context: {
    // auth is populated at runtime by the root route using useAuth()
    auth: undefined!,
  },
  defaultPreload: "intent",
  defaultPreloadStaleTime: 0,
  scrollRestoration: true,
});

// Register the router type globally for full type safety across the app
declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}
