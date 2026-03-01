import { useEffect } from "react";
import { Outlet } from "@tanstack/react-router";
import { useUiStore } from "@/stores/ui.store";
import { Header } from "./Header";
import { Sidebar } from "./Sidebar";

/**
 * Shell — the root application layout wrapping all authenticated pages.
 * Applies the theme class to <html> and composes the sidebar + header.
 */
export function Shell() {
  const { theme, sidebarCollapsed } = useUiStore();

  // Apply theme class to <html> element
  useEffect(() => {
    const root = document.documentElement;
    root.classList.remove("light", "dark");
    if (theme === "system") {
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      root.classList.add(prefersDark ? "dark" : "light");
    } else {
      root.classList.add(theme);
    }
  }, [theme]);

  return (
    <div className="flex min-h-screen bg-background">
      {/* Desktop sidebar */}
      <aside
        className={`hidden md:flex flex-col border-r bg-card transition-all duration-300 ${
          sidebarCollapsed ? "w-16" : "w-64"
        }`}
      >
        <Sidebar />
      </aside>

      {/* Main content area */}
      <div className="flex flex-1 flex-col overflow-hidden">
        <Header />
        <main className="flex-1 overflow-y-auto p-4 md:p-6">
          <Outlet />
        </main>
      </div>

      {/* Mobile bottom tab bar rendered by Sidebar when on small screens */}
    </div>
  );
}
