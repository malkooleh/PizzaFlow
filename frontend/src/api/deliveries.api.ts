import { api } from "./client";
import type { CourierResponse, DeliveryLocation, DeliveryResponse } from "../types/models";

export const deliveriesApi = {
  // ── Delivery queries ──────────────────────────────────────────
  getActiveDeliveries: () =>
    api.get("api/v1/deliveries/active").json<DeliveryResponse[]>(),

  getDelivery: (id: string) =>
    api.get(`api/v1/deliveries/${id}`).json<DeliveryResponse>(),

  getByOrderId: (orderId: string) =>
    api.get(`api/v1/deliveries/order/${orderId}`).json<DeliveryResponse>(),

  trackDelivery: (id: string) =>
    api.get(`api/v1/deliveries/${id}/track`).json<DeliveryResponse>(),

  // ── Delivery status transitions ───────────────────────────────
  markPickedUp: (id: string) =>
    api.post(`api/v1/deliveries/${id}/pickup`).json<DeliveryResponse>(),

  markInTransit: (id: string) =>
    api.post(`api/v1/deliveries/${id}/in-transit`).json<DeliveryResponse>(),

  markArrived: (id: string) =>
    api.post(`api/v1/deliveries/${id}/arrived`).json<DeliveryResponse>(),

  completeDelivery: (id: string, notes?: string) =>
    api
      .post(`api/v1/deliveries/${id}/complete`, {
        searchParams: notes ? { notes } : {},
      })
      .json<DeliveryResponse>(),

  updateDeliveryLocation: (id: string, location: DeliveryLocation) =>
    api
      .post(`api/v1/deliveries/${id}/location`, { json: location })
      .json<void>(),

  // ── Courier queries ───────────────────────────────────────────
  getCourier: (courierId: string) =>
    api.get(`api/v1/couriers/${courierId}`).json<CourierResponse>(),

  getCourierByUserId: (userId: string) =>
    api.get(`api/v1/couriers/user/${userId}`).json<CourierResponse>(),

  getCourierDeliveries: (courierId: string) =>
    api
      .get(`api/v1/couriers/${courierId}/deliveries`)
      .json<DeliveryResponse[]>(),

  // ── Courier status mutations ──────────────────────────────────
  goOnline: (courierId: string, latitude: number, longitude: number) =>
    api
      .post(`api/v1/couriers/${courierId}/online`, {
        searchParams: { latitude, longitude },
      })
      .json<CourierResponse>(),

  goOffline: (courierId: string) =>
    api.post(`api/v1/couriers/${courierId}/offline`).json<CourierResponse>(),

  updateCourierLocation: (courierId: string, location: DeliveryLocation) =>
    api
      .post(`api/v1/couriers/${courierId}/location`, { json: location })
      .json<void>(),
};
