import { create } from "zustand";
import { persist } from "zustand/middleware";

interface UiState {
  /** Currently selected restaurant ID — persisted across sessions. */
  selectedRestaurantId: string | null;
  /** Sidebar collapsed state on desktop. */
  sidebarCollapsed: boolean;
  /** Dark mode preference. */
  theme: "light" | "dark" | "system";
}

interface UiActions {
  setSelectedRestaurant: (restaurantId: string | null) => void;
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setTheme: (theme: UiState["theme"]) => void;
}

export const useUiStore = create<UiState & UiActions>()(
  persist(
    (set) => ({
      selectedRestaurantId: null,
      sidebarCollapsed: false,
      theme: "system",

      setSelectedRestaurant: (restaurantId) =>
        set({ selectedRestaurantId: restaurantId }),

      toggleSidebar: () =>
        set((s) => ({ sidebarCollapsed: !s.sidebarCollapsed })),

      setSidebarCollapsed: (collapsed) =>
        set({ sidebarCollapsed: collapsed }),

      setTheme: (theme) => set({ theme }),
    }),
    {
      name: "pizzaflow-ui",
      // Only persist relevant keys, not volatile UI transitions
      partialize: (s) => ({
        selectedRestaurantId: s.selectedRestaurantId,
        theme: s.theme,
        sidebarCollapsed: s.sidebarCollapsed,
      }),
    }
  )
);
