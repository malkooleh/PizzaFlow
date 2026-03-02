import { api } from "./client";
import { unwrap, unwrapVoid } from "./types";
import type { ApiResponse } from "./types";
import type { InventoryStockLevel, StockAdjustRequest } from "../types/models";

export const inventoryApi = {
  getStockLevels: (restaurantId: string) =>
    api
      .get(`api/v1/inventory/stock/${restaurantId}`)
      .json<ApiResponse<InventoryStockLevel[]>>()
      .then(unwrap),

  getLowStockItems: (restaurantId: string) =>
    api
      .get(`api/v1/inventory/stock/${restaurantId}/low`)
      .json<ApiResponse<InventoryStockLevel[]>>()
      .then(unwrap),

  adjustStock: (payload: StockAdjustRequest) =>
    api
      .post("api/v1/inventory/stock/adjust", { json: payload })
      .json<ApiResponse<void>>()
      .then(unwrapVoid),
};
