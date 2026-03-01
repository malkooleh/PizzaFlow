import { type ReactNode } from "react";
import { render, type RenderResult } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

/** Creates a fresh QueryClient suitable for a single test (no retries, no caching). */
export function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
        staleTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
  });
}

interface WrapperProps {
  children: ReactNode;
}

/** Returns a wrapper component with a fresh QueryClient for each render call. */
export function makeQueryWrapper() {
  const queryClient = createTestQueryClient();
  function Wrapper({ children }: WrapperProps) {
    return (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );
  }
  return { Wrapper, queryClient };
}

/** Renders a component wrapped in a fresh QueryClientProvider. */
export function renderWithQuery(ui: ReactNode): RenderResult & { queryClient: QueryClient } {
  const { Wrapper, queryClient } = makeQueryWrapper();
  return { ...render(ui, { wrapper: Wrapper }), queryClient };
}
