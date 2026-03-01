import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ordersApi, type CreateOrderRequest } from "@/api/orders.api";

// ── Query keys ───────────────────────────────────────────────────────────────

export const orderKeys = {
  all: ["orders"] as const,
  byCustomer: (customerId: number) => [...orderKeys.all, "customer", customerId] as const,
  details: () => [...orderKeys.all, "detail"] as const,
  detail: (id: number) => [...orderKeys.details(), id] as const,
};

// ── Hooks ────────────────────────────────────────────────────────────────────

/**
 * All orders for a customer.
 * Backend: GET /api/v1/orders/customer/{customerId}
 */
export function useOrders(customerId: number | undefined) {
  return useQuery({
    queryKey: orderKeys.byCustomer(customerId!),
    queryFn: () => ordersApi.getOrdersByCustomer(customerId!),
    enabled: customerId != null,
    staleTime: 30_000,
  });
}

/**
 * Single order by ID.
 * Backend: GET /api/v1/orders/{orderId}
 */
export function useOrder(id: number | undefined) {
  return useQuery({
    queryKey: orderKeys.detail(id!),
    queryFn: () => ordersApi.getOrder(id!),
    enabled: id != null,
    staleTime: 15_000,
  });
}

/**
 * Create an order.
 * On success: invalidates the customer order list.
 */
export function useCreateOrder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateOrderRequest) => ordersApi.createOrder(request),
    onSuccess: (_data, vars) => {
      queryClient.invalidateQueries({ queryKey: orderKeys.byCustomer(vars.customerId) });
    },
  });
}

/**
 * Cancel an order.
 */
export function useCancelOrder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: number; reason?: string }) =>
      ordersApi.cancelOrder(id, reason),
    onSuccess: (_data, { id }) => {
      queryClient.invalidateQueries({ queryKey: orderKeys.detail(id) });
    },
  });
}
