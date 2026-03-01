import { useQuery } from "@tanstack/react-query";
import { catalogApi } from "@/api/catalog.api";
import { restaurantsApi } from "@/api/restaurants.api";
import type { MenuCategory } from "@/types/enums";

// ─── Restaurant hooks ──────────────────────────────────────────────────────

export function useRestaurants() {
  return useQuery({
    queryKey: ["restaurants"],
    queryFn: () => restaurantsApi.getAll(),
    staleTime: 5 * 60 * 1000, // restaurants change infrequently
  });
}

export function useRestaurant(id: string | null) {
  return useQuery({
    queryKey: ["restaurant", id],
    queryFn: () => restaurantsApi.getById(id!),
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
  });
}

// ─── Menu hooks ──────────────────────────────────────────────────────────────

/**
 * Full menu for a restaurant. Stays fresh for 5 minutes.
 */
export function useMenu(restaurantId: string | null) {
  return useQuery({
    queryKey: ["menu", restaurantId],
    queryFn: () => catalogApi.getMenu(restaurantId!),
    enabled: !!restaurantId,
    staleTime: 5 * 60 * 1000,
  });
}

/**
 * Menu filtered by category.
 */
export function useMenuByCategory(restaurantId: string | null, category: MenuCategory | null) {
  return useQuery({
    queryKey: ["menu", restaurantId, "category", category],
    queryFn: () => catalogApi.getMenuByCategory(restaurantId!, category!),
    enabled: !!restaurantId && !!category,
    staleTime: 5 * 60 * 1000,
  });
}

/**
 * Featured / highlighted items for a restaurant.
 */
export function useFeaturedItems(restaurantId: string | null) {
  return useQuery({
    queryKey: ["menu", restaurantId, "featured"],
    queryFn: () => catalogApi.getFeaturedItems(restaurantId!),
    enabled: !!restaurantId,
    staleTime: 5 * 60 * 1000,
  });
}

/**
 * Debounced full-text search.
 * Pass `null` / empty string to skip the query.
 */
export function useSearchMenu(restaurantId: string | null, query: string | null) {
  return useQuery({
    queryKey: ["menu", restaurantId, "search", query],
    queryFn: () => catalogApi.searchMenu(restaurantId!, query!),
    enabled: !!restaurantId && !!query && query.trim().length >= 2,
    staleTime: 60 * 1000,
    placeholderData: (prev) => prev,
  });
}

/**
 * Single menu item detail.
 */
export function useMenuItem(id: string | null) {
  return useQuery({
    queryKey: ["menu-item", id],
    queryFn: () => catalogApi.getMenuItem(id!),
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
  });
}
