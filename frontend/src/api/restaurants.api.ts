import { api } from "./client";
import type { ApiResponse, PageResponse } from "./types";
import { unwrap } from "./types";
import type { Restaurant } from "@/types/models";

export const restaurantsApi = {
  /**
   * List all active restaurants.
   */
  getAll: () =>
    api
      .get("api/v1/restaurants")
      .json<ApiResponse<Restaurant[]>>()
      .then(unwrap),

  /**
   * Get a single restaurant by ID.
   */
  getById: (id: string) =>
    api
      .get(`api/v1/restaurants/${id}`)
      .json<ApiResponse<Restaurant>>()
      .then(unwrap),

  /**
   * Paginated restaurant listing (for manager/admin views).
   */
  list: (page = 0, size = 20) =>
    api
      .get("api/v1/restaurants", { searchParams: { page, size } })
      .json<PageResponse<Restaurant>>(),
};
