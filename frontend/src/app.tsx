import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { RouterProvider } from "@tanstack/react-router";
import { AuthProvider, useAuth } from "react-oidc-context";
import { Toaster } from "sonner";
import { userManager } from "@/lib/auth";
import { router } from "@/router";

/**
 * TanStack Query client — global cache configuration.
 * staleTime: 30s prevents redundant refetches for rapidly navigating users.
 * retry: 1 — one retry on failed requests before surfacing the error.
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,
    },
  },
});

/**
 * Inner component that can call useAuth() (only valid inside AuthProvider)
 * and passes the auth context into the TanStack Router.
 */
function InnerApp() {
  const auth = useAuth();
  return <RouterProvider router={router} context={{ auth }} />;
}

export function App() {
  return (
    <AuthProvider userManager={userManager}>
      <QueryClientProvider client={queryClient}>
        <InnerApp />
        <Toaster position="top-right" richColors closeButton />
        <ReactQueryDevtools initialIsOpen={false} />
      </QueryClientProvider>
    </AuthProvider>
  );
}
