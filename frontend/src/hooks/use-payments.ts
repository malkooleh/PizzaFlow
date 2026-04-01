import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { paymentsApi, type ProcessPaymentRequest } from "@/api/payments.api";
import { orderKeys } from "./use-orders";

// ── Query keys ───────────────────────────────────────────────────────────────

export const paymentKeys = {
  all: ["payments"] as const,
  byOrder: (orderId: number) => [...paymentKeys.all, "order", orderId] as const,
  byCustomer: (customerId: number) => [...paymentKeys.all, "customer", customerId] as const,
};

// ── Hooks ────────────────────────────────────────────────────────────────────

/**
 * Fetch payment for a specific order.
 * Backend: GET /api/v1/payments/order/{orderId}
 */
export function usePaymentByOrder(orderId: number | undefined) {
  return useQuery({
    queryKey: paymentKeys.byOrder(orderId!),
    queryFn: () => paymentsApi.getPaymentByOrder(orderId!),
    enabled: orderId != null,
    staleTime: 30_000,
  });
}

/**
 * All payments for a customer.
 * Backend: GET /api/v1/payments/customer/{customerId}
 */
export function usePaymentsByCustomer(customerId: number | undefined) {
  return useQuery({
    queryKey: paymentKeys.byCustomer(customerId!),
    queryFn: () => paymentsApi.getPaymentsByCustomer(customerId!),
    enabled: customerId != null,
    staleTime: 60_000,
  });
}

/**
 * Process payment mutation.
 * Backend: POST /api/v1/payments
 * On success: invalidates order detail (status may have changed).
 */
export function useProcessPayment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: ProcessPaymentRequest) => paymentsApi.processPayment(request),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: orderKeys.detail(variables.orderId) });
      void queryClient.invalidateQueries({ queryKey: paymentKeys.byOrder(variables.orderId) });
      void queryClient.invalidateQueries({ queryKey: paymentKeys.byCustomer(variables.customerId) });
    },
  });
}
