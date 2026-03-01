import { api } from "./client";
import type { ApiResponse } from "./types";
import { unwrap } from "./types";
import type { PaymentResponse } from "@/types/models";
import type { PaymentMethodType } from "@/types/enums";

// ── Request shapes ───────────────────────────────────────────────────────────

export interface ProcessPaymentRequest {
  orderId: number;
  /** Numeric DB customer ID required by backend */
  customerId: number;
  /** Total amount to charge */
  amount: number;
  currency?: string;
  paymentMethodType: PaymentMethodType;
  /** Optional: last-4 or masked card number displayed to user */
  cardLastFour?: string;
  cardHolderName?: string;
  expiryMonth?: number;
  expiryYear?: number;
  cvv?: string;
  savePaymentMethod?: boolean;
}

// ── API ──────────────────────────────────────────────────────────────────────

export const paymentsApi = {
  /**
   * Process a new payment.
   * Backend: POST /api/v1/payments
   */
  processPayment: (request: ProcessPaymentRequest) =>
    api
      .post("api/v1/payments", { json: { ...request, currency: request.currency ?? "USD" } })
      .json<ApiResponse<PaymentResponse>>()
      .then(unwrap),

  /**
   * Get payment by transaction ID.
   * Backend: GET /api/v1/payments/{transactionId}
   */
  getPayment: (transactionId: string) =>
    api
      .get(`api/v1/payments/${transactionId}`)
      .json<ApiResponse<PaymentResponse>>()
      .then(unwrap),

  /**
   * Get payment for a specific order.
   * Backend: GET /api/v1/payments/order/{orderId}
   */
  getPaymentByOrder: (orderId: number) =>
    api
      .get(`api/v1/payments/order/${orderId}`)
      .json<ApiResponse<PaymentResponse>>()
      .then(unwrap),

  /**
   * Get all payments for a customer.
   * Backend: GET /api/v1/payments/customer/{customerId}
   */
  getPaymentsByCustomer: (customerId: number) =>
    api
      .get(`api/v1/payments/customer/${customerId}`)
      .json<ApiResponse<PaymentResponse[]>>()
      .then(unwrap),
};
