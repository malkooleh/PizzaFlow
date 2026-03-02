import { api } from "./client";
import type { AvailabilityResponse, BookingResponse, BookingRestaurant } from "@/types/models";
import type { TableType } from "@/types/enums";

// ── Request shapes ───────────────────────────────────────────────────────────

/**
 * Mirrors the backend BookingRequest record.
 * Note: booking-service uses UUID strings for IDs — NOT numeric Long IDs.
 * Use `auth.user?.sub` (the Keycloak subject UUID) as customerId.
 */
export interface CreateBookingRequest {
  /** Keycloak subject UUID — NOT the numeric DB id used by order/payment services */
  customerId: string;
  restaurantId: string;
  /** ISO-8601 local datetime e.g. "2026-03-15T19:00:00" */
  reservationTime: string;
  partySize: number;
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  specialRequests?: string;
  /** Optional seating preference */
  preferredTableType?: TableType;
  /** Optional specific table UUID */
  preferredTableId?: string;
  /** Defaults to restaurant's bookingSlotDurationMinutes when omitted */
  durationMinutes?: number;
}

// ── API ──────────────────────────────────────────────────────────────────────

/**
 * Booking service client.
 *
 * IMPORTANT: This service returns raw DTOs directly — NOT wrapped in
 * `ApiResponse<T>`. Do not call `.then(unwrap)` on these methods.
 */
export const bookingsApi = {
  /**
   * Check table availability for a given restaurant/date/party-size.
   * Backend: GET /api/v1/bookings/availability
   */
  checkAvailability: (restaurantId: string, date: string, partySize: number) =>
    api
      .get("api/v1/bookings/availability", {
        searchParams: { restaurantId, date, partySize },
      })
      .json<AvailabilityResponse>(),

  /**
   * Create a new table reservation.
   * Backend: POST /api/v1/bookings  → 201 Created
   */
  createBooking: (request: CreateBookingRequest) =>
    api.post("api/v1/bookings", { json: request }).json<BookingResponse>(),

  /**
   * Get a single booking by its UUID.
   * Backend: GET /api/v1/bookings/{bookingId}
   */
  getBooking: (id: string) =>
    api.get(`api/v1/bookings/${id}`).json<BookingResponse>(),

  /**
   * Get a booking by its human-readable booking number (e.g. "BK-20260301-001").
   * Backend: GET /api/v1/bookings/number/{bookingNumber}
   */
  getBookingByNumber: (bookingNumber: string) =>
    api.get(`api/v1/bookings/number/${bookingNumber}`).json<BookingResponse>(),

  /**
   * Get all bookings for a customer.
   * Backend: GET /api/v1/bookings/customer/{customerId}
   */
  getCustomerBookings: (customerId: string) =>
    api.get(`api/v1/bookings/customer/${customerId}`).json<BookingResponse[]>(),

  /**
   * Cancel a booking, optionally with a reason.
   * Backend: POST /api/v1/bookings/{bookingId}/cancel?reason=...
   */
  cancelBooking: (id: string, reason?: string) =>
    api
      .post(`api/v1/bookings/${id}/cancel`, {
        searchParams: reason ? { reason } : {},
      })
      .json<BookingResponse>(),

  /**
   * List restaurants that have booking capacity set up.
   * Backend: GET /api/v1/restaurants  (booking-service, NOT catalog-service)
   * Returns a raw array — NOT wrapped in ApiResponse<T>.
   */
  getRestaurants: () =>
    api.get("api/v1/restaurants").json<BookingRestaurant[]>(),
};
