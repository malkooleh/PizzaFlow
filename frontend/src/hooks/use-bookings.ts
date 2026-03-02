import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { bookingsApi, type CreateBookingRequest } from "@/api/bookings.api";

// ── Query key factory ────────────────────────────────────────────────────────

export const bookingKeys = {
  all: ["bookings"] as const,
  customer: (customerId: string) =>
    [...bookingKeys.all, "customer", customerId] as const,
  detail: (id: string) => [...bookingKeys.all, id] as const,
  byNumber: (bookingNumber: string) =>
    [...bookingKeys.all, "number", bookingNumber] as const,
  availability: (restaurantId: string, date: string, partySize: number) =>
    [...bookingKeys.all, "availability", restaurantId, date, partySize] as const,
  restaurants: () => ["booking-restaurants"] as const,
};

// ── Queries ──────────────────────────────────────────────────────────────────

/**
 * List of restaurants that have booking support configured.
 * Used to populate the restaurant selector in BookingForm.
 */
export function useBookingRestaurants() {
  return useQuery({
    queryKey: bookingKeys.restaurants(),
    queryFn: () => bookingsApi.getRestaurants(),
    staleTime: 5 * 60 * 1000, // restaurant list rarely changes
  });
}

/**
 * Availability slots for a given restaurant/date/party-size combination.
 * Query is disabled until all three parameters are provided.
 */
export function useAvailability(
  restaurantId: string | undefined,
  date: string | undefined,
  partySize: number,
) {
  return useQuery({
    queryKey: bookingKeys.availability(restaurantId ?? "", date ?? "", partySize),
    queryFn: () => bookingsApi.checkAvailability(restaurantId!, date!, partySize),
    enabled: !!restaurantId && !!date && partySize > 0,
  });
}

/**
 * All bookings for a customer (identified by Keycloak UUID sub).
 */
export function useBookings(customerId: string | undefined) {
  return useQuery({
    queryKey: bookingKeys.customer(customerId ?? ""),
    queryFn: () => bookingsApi.getCustomerBookings(customerId!),
    enabled: !!customerId,
  });
}

/**
 * Single booking by UUID.
 */
export function useBooking(id: string | undefined) {
  return useQuery({
    queryKey: bookingKeys.detail(id ?? ""),
    queryFn: () => bookingsApi.getBooking(id!),
    enabled: !!id,
  });
}

/**
 * Single booking by human-readable booking number.
 */
export function useBookingByNumber(bookingNumber: string | undefined) {
  return useQuery({
    queryKey: bookingKeys.byNumber(bookingNumber ?? ""),
    queryFn: () => bookingsApi.getBookingByNumber(bookingNumber!),
    enabled: !!bookingNumber,
  });
}

// ── Mutations ────────────────────────────────────────────────────────────────

/**
 * Create a new table reservation. On success, invalidates the customer's
 * booking list so the new entry appears immediately.
 */
export function useCreateBooking() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CreateBookingRequest) =>
      bookingsApi.createBooking(request),
    onSuccess: (booking) => {
      queryClient.invalidateQueries({
        queryKey: bookingKeys.customer(booking.customerId),
      });
      // Seed the detail cache so navigating to it is instant
      queryClient.setQueryData(bookingKeys.detail(booking.id), booking);
    },
  });
}

/**
 * Cancel an existing booking. Re-fetches the customer's list and updates
 * the detail cache with the returned (cancelled) booking.
 */
export function useCancelBooking() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason?: string }) =>
      bookingsApi.cancelBooking(id, reason),
    onSuccess: (booking) => {
      queryClient.invalidateQueries({
        queryKey: bookingKeys.customer(booking.customerId),
      });
      queryClient.setQueryData(bookingKeys.detail(booking.id), booking);
    },
  });
}
