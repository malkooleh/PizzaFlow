import { api } from "./client";
import type { ApiResponse } from "./types";
import { unwrap, unwrapVoid } from "./types";
import type { MenuItem } from "@/types/models";
import type { MenuCategory } from "@/types/enums";

// ── Request shape ────────────────────────────────────────────────────────────

export interface MenuItemUpdateRequest {
  name: string;
  description?: string;
  price: number;
  category: MenuCategory;
  imageUrl?: string;
  allergens?: string[];
  isAvailable?: boolean;
  isFeatured?: boolean;
  preparationTimeMinutes?: number;
}

export const catalogApi = {
  /**
   * Get the full menu for a restaurant.
   */
  getMenu: (restaurantId: string) =>
    api
      .get(`api/v1/catalog/menu/${restaurantId}`)
      .json<ApiResponse<MenuItem[]>>()
      .then(unwrap),

  /**
   * Get menu items filtered by category.
   */
  getMenuByCategory: (restaurantId: string, category: MenuCategory) =>
    api
      .get(`api/v1/catalog/menu/${restaurantId}`, { searchParams: { category } })
      .json<ApiResponse<MenuItem[]>>()
      .then(unwrap),

  /**
   * Get featured / highlighted items for a restaurant.
   */
  getFeaturedItems: (restaurantId: string) =>
    api
      .get(`api/v1/catalog/menu/${restaurantId}/featured`)
      .json<ApiResponse<MenuItem[]>>()
      .then(unwrap),

  /**
   * Full-text search within a restaurant's menu.
   */
  searchMenu: (restaurantId: string, query: string) =>
    api
      .get(`api/v1/catalog/menu/${restaurantId}/search`, { searchParams: { query } })
      .json<ApiResponse<MenuItem[]>>()
      .then(unwrap),

  /**
   * Get a single menu item by its UUID.
   */
  getMenuItem: (id: string) =>
    api
      .get(`api/v1/catalog/items/${id}`)
      .json<ApiResponse<MenuItem>>()
      .then(unwrap),

  // ── Manager CRUD ─────────────────────────────────────────────────────────

  /**
   * Create a new menu item for a restaurant.
   * Backend: POST /api/v1/catalog/menu/{restaurantId}/items
   */
  createMenuItem: (restaurantId: string, data: MenuItemUpdateRequest) =>
    api
      .post(`api/v1/catalog/items`, { json: { ...data, restaurantId } })
      .json<ApiResponse<MenuItem>>()
      .then(unwrap),

  /**
   * Update an existing menu item.
   * Backend: PUT /api/v1/catalog/menu/{restaurantId}/items/{itemId}
   */
  updateMenuItem: (
    restaurantId: string,
    itemId: string,
    data: Partial<MenuItemUpdateRequest>,
  ) =>
    api
      .put(`api/v1/catalog/items/${itemId}`, { json: { ...data, restaurantId } })
      .json<ApiResponse<MenuItem>>()
      .then(unwrap),

  /**
   * Delete a menu item.
   * Backend: DELETE /api/v1/catalog/menu/{restaurantId}/items/{itemId}
   */
  deleteMenuItem: (restaurantId: string, itemId: string) =>
    api
      .delete(`api/v1/catalog/items/${itemId}`)
      .json<ApiResponse<void>>()
      .then(unwrapVoid),
};
