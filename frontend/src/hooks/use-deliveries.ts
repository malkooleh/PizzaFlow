import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { deliveriesApi } from "@/api/deliveries.api";
import type { DeliveryLocation } from "@/types/models";

// ── Query keys ───────────────────────────────────────────────────────────────

export const deliveryKeys = {
  all: ["deliveries"] as const,
  active: () => [...deliveryKeys.all, "active"] as const,
  detail: (id: string) => [...deliveryKeys.all, "detail", id] as const,
  byOrder: (orderId: string) => [...deliveryKeys.all, "order", orderId] as const,
  couriers: ["couriers"] as const,
  courier: (courierId: string) => [...deliveryKeys.couriers, courierId] as const,
  courierByUser: (userId: string) => [...deliveryKeys.couriers, "user", userId] as const,
  courierDeliveries: (courierId: string) =>
    [...deliveryKeys.couriers, courierId, "deliveries"] as const,
};

// ── Delivery queries ─────────────────────────────────────────────────────────

/** All active deliveries — polls every 10 s. */
export function useActiveDeliveries() {
  return useQuery({
    queryKey: deliveryKeys.active(),
    queryFn: () => deliveriesApi.getActiveDeliveries(),
    staleTime: 5_000,
    refetchInterval: 10_000,
  });
}

/** Single delivery by ID — polls every 10 s while active. */
export function useDelivery(id: string | undefined) {
  return useQuery({
    queryKey: deliveryKeys.detail(id!),
    queryFn: () => deliveriesApi.getDelivery(id!),
    enabled: id != null,
    staleTime: 5_000,
    refetchInterval: 10_000,
  });
}

/** Delivery linked to an order. */
export function useDeliveryByOrder(orderId: string | undefined) {
  return useQuery({
    queryKey: deliveryKeys.byOrder(orderId!),
    queryFn: () => deliveriesApi.getByOrderId(orderId!),
    enabled: orderId != null,
    staleTime: 10_000,
    refetchInterval: 15_000,
  });
}

// ── Courier queries ──────────────────────────────────────────────────────────

/** Courier profile by courier ID. */
export function useCourier(courierId: string | undefined) {
  return useQuery({
    queryKey: deliveryKeys.courier(courierId!),
    queryFn: () => deliveriesApi.getCourier(courierId!),
    enabled: courierId != null,
    staleTime: 60_000,
  });
}

/** Courier profile by Keycloak user ID. */
export function useCourierProfile(userId: string | undefined) {
  return useQuery({
    queryKey: deliveryKeys.courierByUser(userId!),
    queryFn: () => deliveriesApi.getCourierByUserId(userId!),
    enabled: userId != null,
    staleTime: 60_000,
  });
}

/** All deliveries assigned to a courier — polls every 15 s. */
export function useCourierDeliveries(courierId: string | undefined) {
  return useQuery({
    queryKey: deliveryKeys.courierDeliveries(courierId!),
    queryFn: () => deliveriesApi.getCourierDeliveries(courierId!),
    enabled: courierId != null,
    staleTime: 5_000,
    refetchInterval: 15_000,
  });
}

// ── Courier status mutations ─────────────────────────────────────────────────

export function useGoOnline() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      courierId,
      latitude,
      longitude,
    }: {
      courierId: string;
      latitude: number;
      longitude: number;
    }) => deliveriesApi.goOnline(courierId, latitude, longitude),
    onSuccess: (_data, { courierId }) => {
      qc.invalidateQueries({ queryKey: deliveryKeys.courier(courierId) });
    },
  });
}

export function useGoOffline() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (courierId: string) => deliveriesApi.goOffline(courierId),
    onSuccess: (_data, courierId) => {
      qc.invalidateQueries({ queryKey: deliveryKeys.courier(courierId) });
    },
  });
}

export function useUpdateCourierLocation() {
  return useMutation({
    mutationFn: ({
      courierId,
      location,
    }: {
      courierId: string;
      location: DeliveryLocation;
    }) => deliveriesApi.updateCourierLocation(courierId, location),
  });
}

// ── Delivery status mutations ────────────────────────────────────────────────

function useDeliveryTransition(
  mutationFn: (id: string) => Promise<unknown>,
) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => mutationFn(id),
    onSuccess: (_data, id) => {
      qc.invalidateQueries({ queryKey: deliveryKeys.detail(id) });
      qc.invalidateQueries({ queryKey: deliveryKeys.active() });
    },
  });
}

export const useMarkPickedUp = () =>
  useDeliveryTransition((id) => deliveriesApi.markPickedUp(id));

export const useMarkInTransit = () =>
  useDeliveryTransition((id) => deliveriesApi.markInTransit(id));

export const useMarkArrived = () =>
  useDeliveryTransition((id) => deliveriesApi.markArrived(id));

export function useCompleteDelivery() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, notes }: { id: string; notes?: string }) =>
      deliveriesApi.completeDelivery(id, notes),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: deliveryKeys.detail(id) });
      qc.invalidateQueries({ queryKey: deliveryKeys.active() });
    },
  });
}

export function useUpdateDeliveryLocation() {
  return useMutation({
    mutationFn: ({
      id,
      location,
    }: {
      id: string;
      location: DeliveryLocation;
    }) => deliveriesApi.updateDeliveryLocation(id, location),
  });
}
