/**
 * API type definitions shared across all API modules.
 *
 * ApiResponse<T> mirrors the com.pizzaflow.common.dto.ApiResponse<T> Java class.
 * Services that return raw DTOs (booking, delivery, notification) bypass this wrapper.
 */

export interface ErrorDetail {
  code: string | null;
  field: string | null;
  rejectedValue: unknown;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string | null;
  error: string | null;
  errorDetails?: ErrorDetail[] | null;
  timestamp: string;
  traceId: string | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

/**
 * Extracts the data payload from an ApiResponse<T>.
 * Throws an Error with the server error message if success is false.
 */
export function unwrap<T>(response: ApiResponse<T>): T {
  if (!response.success || response.data === null) {
    throw new Error(response.error ?? response.message ?? "API error");
  }
  return response.data;
}

/**
 * Unwraps a paged ApiResponse<PageResponse<T>>.
 */
export function unwrapPage<T>(response: ApiResponse<PageResponse<T>>): PageResponse<T> {
  return unwrap(response);
}
