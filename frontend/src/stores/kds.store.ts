import { create } from "zustand";
import { persist } from "zustand/middleware";

export type KdsLayout = "kanban" | "list" | "grid";

interface KdsState {
  /** Multi-column Kanban (default), single list, or compact grid. */
  layout: KdsLayout;
  /** Play audio alert sound when a new order arrives. */
  audioAlertsEnabled: boolean;
  /** Auto-scroll the board when new cards arrive. */
  autoScroll: boolean;
  /** Selected restaurantId for the KDS view. */
  restaurantId: string | null;
}

interface KdsActions {
  setLayout: (layout: KdsLayout) => void;
  toggleAudioAlerts: () => void;
  toggleAutoScroll: () => void;
  setRestaurantId: (id: string) => void;
}

export const useKdsStore = create<KdsState & KdsActions>()(
  persist(
    (set) => ({
      layout: "kanban",
      audioAlertsEnabled: true,
      autoScroll: true,
      restaurantId: null,

      setLayout: (layout) => set({ layout }),
      toggleAudioAlerts: () => set((s) => ({ audioAlertsEnabled: !s.audioAlertsEnabled })),
      toggleAutoScroll: () => set((s) => ({ autoScroll: !s.autoScroll })),
      setRestaurantId: (restaurantId) => set({ restaurantId }),
    }),
    { name: "pizzaflow-kds" }
  )
);
