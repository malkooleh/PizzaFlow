import type { ApiResponse, PageResponse } from "@/api/types";

/** Convenience re-exports used throughout the app. */
export type { ApiResponse, PageResponse };

/** Standard paginated query params passed to API calls. */
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

/** Standard list filter/search params for order history etc. */
export interface ListFilters extends PaginationParams {
  status?: string;
  q?: string;
  from?: string;
  to?: string;
}
