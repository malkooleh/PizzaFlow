import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { inventoryApi } from "@/api/inventory.api";
import type { StockAdjustRequest } from "@/types/models";
import { bookingsApi } from "@/api/bookings.api";
import { catalogApi } from "@/api/catalog.api";
import type { MenuItemUpdateRequest } from "@/api/catalog.api";

// ── Query keys ───────────────────────────────────────────────────────────────

export const managerKeys = {
  inventory: (restaurantId: string) =>
    ["inventory", "stock", restaurantId] as const,
  lowStock: (restaurantId: string) =>
    ["inventory", "stock", restaurantId, "low"] as const,
  todayBookings: (restaurantId: string) =>
    ["bookings", "restaurant", restaurantId, "today"] as const,
};

// ── Inventory queries ────────────────────────────────────────────────────────

/** All stock levels for a restaurant. */
export function useStockLevels(restaurantId: string | undefined) {
  return useQuery({
    queryKey: managerKeys.inventory(restaurantId!),
    queryFn: () => inventoryApi.getStockLevels(restaurantId!),
    enabled: restaurantId != null,
    staleTime: 30_000,
    refetchInterval: 60_000,
  });
}

/** Only items below the minimum threshold. */
export function useLowStockItems(restaurantId: string | undefined) {
  return useQuery({
    queryKey: managerKeys.lowStock(restaurantId!),
    queryFn: () => inventoryApi.getLowStockItems(restaurantId!),
    enabled: restaurantId != null,
    staleTime: 30_000,
    refetchInterval: 60_000,
  });
}

/** Adjust stock for a single ingredient. */
export function useAdjustStock() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: StockAdjustRequest) =>
      inventoryApi.adjustStock(payload),
    onSuccess: (_data, { restaurantId }) => {
      qc.invalidateQueries({
        queryKey: managerKeys.inventory(restaurantId),
      });
      qc.invalidateQueries({
        queryKey: managerKeys.lowStock(restaurantId),
      });
    },
  });
}

// ── Today's bookings ─────────────────────────────────────────────────────────

/** Bookings for today at a specific restaurant. */
export function useTodayBookings(restaurantId: string | undefined) {
  return useQuery({
    queryKey: managerKeys.todayBookings(restaurantId!),
    queryFn: () => bookingsApi.getTodayBookings(restaurantId!),
    enabled: restaurantId != null,
    staleTime: 15_000,
    refetchInterval: 30_000,
  });
}

// ── Menu CRUD mutations ──────────────────────────────────────────────────────

/** Create a new menu item. Invalidates the full menu for the restaurant. */
export function useCreateMenuItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      restaurantId,
      data,
    }: {
      restaurantId: string;
      data: MenuItemUpdateRequest;
    }) => catalogApi.createMenuItem(restaurantId, data),
    onSuccess: (_data, { restaurantId }) => {
      qc.invalidateQueries({ queryKey: ["menu", restaurantId] });
    },
  });
}

/** Update an existing menu item. */
export function useUpdateMenuItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      restaurantId,
      itemId,
      data,
    }: {
      restaurantId: string;
      itemId: string;
      data: Partial<MenuItemUpdateRequest>;
    }) => catalogApi.updateMenuItem(restaurantId, itemId, data),
    onSuccess: (_data, { restaurantId }) => {
      qc.invalidateQueries({ queryKey: ["menu", restaurantId] });
    },
  });
}

/** Delete a menu item. */
export function useDeleteMenuItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      restaurantId,
      itemId,
    }: {
      restaurantId: string;
      itemId: string;
    }) => catalogApi.deleteMenuItem(restaurantId, itemId),
    onSuccess: (_data, { restaurantId }) => {
      qc.invalidateQueries({ queryKey: ["menu", restaurantId] });
    },
  });
}

// ── Booking state mutations ──────────────────────────────────────────────────

function useBookingTransition(
  mutationFn: (id: string) => Promise<unknown>,
  restaurantId: string | undefined,
) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (bookingId: string) => mutationFn(bookingId),
    onSuccess: () => {
      if (restaurantId) {
        qc.invalidateQueries({
          queryKey: managerKeys.todayBookings(restaurantId),
        });
      }
    },
  });
}

export const useConfirmBooking = (restaurantId?: string) =>
  useBookingTransition((id) => bookingsApi.confirmBooking(id), restaurantId);

export const useSeatBooking = (restaurantId?: string) =>
  useBookingTransition((id) => bookingsApi.seatBooking(id), restaurantId);

export const useNoShowBooking = (restaurantId?: string) =>
  useBookingTransition((id) => bookingsApi.markNoShow(id), restaurantId);
