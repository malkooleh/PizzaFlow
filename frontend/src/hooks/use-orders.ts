import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ordersApi, type CreateOrderRequest } from "@/api/orders.api";
import { OrderStatus } from "@/types/enums";

// ── Query keys ───────────────────────────────────────────────────────────────

export const orderKeys = {
  all: ["orders"] as const,
  byCustomer: (customerId: number) => [...orderKeys.all, "customer", customerId] as const,
  details: () => [...orderKeys.all, "detail"] as const,
  detail: (id: number) => [...orderKeys.details(), id] as const,
  byNumber: (orderNumber: string) => [...orderKeys.all, "number", orderNumber] as const,
};

/** Statuses that are still progressing — used to gate polling. */
export const ACTIVE_ORDER_STATUSES = new Set<OrderStatus>([
  OrderStatus.PENDING,
  OrderStatus.CONFIRMED,
  OrderStatus.PREPARING,
  OrderStatus.READY,
  OrderStatus.PICKED_UP,
  OrderStatus.OUT_FOR_DELIVERY,
]);

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
      void queryClient.invalidateQueries({ queryKey: orderKeys.byCustomer(vars.customerId) });
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
      void queryClient.invalidateQueries({ queryKey: orderKeys.detail(id) });
    },
  });
}

/**
 * Look up an order by its human-readable order number (e.g. "PF-01001").
 * Backend: GET /api/v1/orders/number/{orderNumber}
 */
export function useOrderByNumber(orderNumber: string | undefined) {
  return useQuery({
    queryKey: orderKeys.byNumber(orderNumber!),
    queryFn: () => ordersApi.getOrderByNumber(orderNumber!),
    enabled: !!orderNumber,
    staleTime: 15_000,
  });
}

/**
 * Live-tracking hook — polls every 5 s while the order is in an active status.
 * Stops polling once the order is delivered, completed, or cancelled.
 */
export function useActiveOrderTracking(id: number | undefined) {
  return useQuery({
    queryKey: orderKeys.detail(id!),
    queryFn: () => ordersApi.getOrder(id!),
    enabled: id != null,
    staleTime: 5_000,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status && ACTIVE_ORDER_STATUSES.has(status) ? 5_000 : false;
    },
  });
}
