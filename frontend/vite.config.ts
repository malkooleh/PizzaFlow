import path from "path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import { TanStackRouterVite } from "@tanstack/router-plugin/vite";

export default defineConfig({
  plugins: [
    // TanStack Router plugin must come before React plugin
    TanStackRouterVite({ routesDirectory: "./src/routes", generatedRouteTree: "./src/routeTree.gen.ts" }),
    react(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 4200,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      // WebSocket proxy to kitchen service
      "/ws": {
        target: "ws://localhost:8084",
        ws: true,
        changeOrigin: true,
      },
    },
  },
  build: {
    // Code splitting: each route chunk is separate for better caching
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ["react", "react-dom"],
          tanstack: ["@tanstack/react-router", "@tanstack/react-query"],
          ui: ["lucide-react", "sonner"],
          charts: ["recharts"],
          maps: ["mapbox-gl", "react-map-gl"],
        },
      },
    },
  },
});
