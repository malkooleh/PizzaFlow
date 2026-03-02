import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    environmentOptions: {
      jsdom: {
        // Must match ky's prefixUrl (VITE_API_URL) so that MSW relative-path
        // handlers (e.g. "/api/v1/orders/:id") resolve to the same origin.
        url: "http://localhost:8080",
      },
    },
    globals: true,
    setupFiles: ["./src/test/setup.ts"],
    coverage: {
      provider: "v8",
      reporter: ["text", "html", "lcov"],
      exclude: [
        "node_modules/",
        "src/routeTree.gen.ts",
        "src/test/",
        "src/mocks/",
        "**/*.config.*",
        "**/*.d.ts",
      ],
      thresholds: {
        lines: 60,
      },
    },
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});
