import { create } from "zustand";
import { persist } from "zustand/middleware";

export interface CartModifier {
  modifierId: string;
  name: string;
  additionalPrice: number;
}

export interface CartItem {
  /** Cart-level unique key: menuItemId + sorted modifier IDs */
  key: string;
  menuItemId: string;
  menuItemName: string;
  basePrice: number;
  quantity: number;
  selectedModifiers: CartModifier[];
  specialInstructions?: string;
  imageUrl?: string;
}

interface CartState {
  restaurantId: string | null;
  items: CartItem[];
}

interface CartActions {
  addItem: (restaurantId: string, item: Omit<CartItem, "key">) => void;
  removeItem: (key: string) => void;
  updateQuantity: (key: string, quantity: number) => void;
  clear: () => void;
  /** Returns true if the new restaurant differs from the current one. */
  wouldClearCart: (restaurantId: string) => boolean;
}

interface CartSelectors {
  totalItems: () => number;
  subtotal: () => number;
  tax: () => number;
  deliveryFee: () => number;
  total: () => number;
}

function buildKey(menuItemId: string, modifiers: CartModifier[]): string {
  const modKey = modifiers.map((m) => m.modifierId).sort().join("|");
  return `${menuItemId}${modKey ? `:${modKey}` : ""}`;
}

function itemLineTotal(item: CartItem): number {
  const modifiersTotal = item.selectedModifiers.reduce((s, m) => s + m.additionalPrice, 0);
  return (item.basePrice + modifiersTotal) * item.quantity;
}

export const useCartStore = create<CartState & CartActions & CartSelectors>()(
  persist(
    (set, get) => ({
      restaurantId: null,
      items: [],

      addItem: (restaurantId, item) => {
        set((state) => {
          // If different restaurant → replace the whole cart
          if (state.restaurantId && state.restaurantId !== restaurantId) {
            const key = buildKey(item.menuItemId, item.selectedModifiers);
            return { restaurantId, items: [{ ...item, key }] };
          }

          const key = buildKey(item.menuItemId, item.selectedModifiers);
          const existing = state.items.find((i) => i.key === key);

          if (existing) {
            return {
              restaurantId,
              items: state.items.map((i) =>
                i.key === key ? { ...i, quantity: i.quantity + item.quantity } : i
              ),
            };
          }

          return { restaurantId, items: [...state.items, { ...item, key }] };
        });
      },

      removeItem: (key) =>
        set((state) => {
          const items = state.items.filter((i) => i.key !== key);
          return { items, restaurantId: items.length === 0 ? null : state.restaurantId };
        }),

      updateQuantity: (key, quantity) =>
        set((state) => {
          if (quantity <= 0) {
            const items = state.items.filter((i) => i.key !== key);
            return { items, restaurantId: items.length === 0 ? null : state.restaurantId };
          }
          return {
            items: state.items.map((i) => (i.key === key ? { ...i, quantity } : i)),
          };
        }),

      clear: () => set({ items: [], restaurantId: null }),

      wouldClearCart: (restaurantId) => {
        const state = get();
        return state.restaurantId !== null && state.restaurantId !== restaurantId;
      },

      totalItems: () => get().items.reduce((s, i) => s + i.quantity, 0),

      subtotal: () => get().items.reduce((s, i) => s + itemLineTotal(i), 0),

      tax: () => {
        const sub = get().subtotal();
        return Math.round(sub * 0.1 * 100) / 100; // 10% tax
      },

      deliveryFee: () => (get().items.length > 0 ? 2.99 : 0),

      total: () => {
        const sub = get().subtotal();
        return Math.round((sub + get().tax() + get().deliveryFee()) * 100) / 100;
      },
    }),
    {
      name: "pizzaflow-cart",
    }
  )
);
