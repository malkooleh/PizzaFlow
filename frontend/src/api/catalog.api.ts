import { api } from "./client";
import type { ApiResponse } from "./types";
import { unwrap } from "./types";
import type { MenuItem } from "@/types/models";
import type { MenuCategory } from "@/types/enums";

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
};
