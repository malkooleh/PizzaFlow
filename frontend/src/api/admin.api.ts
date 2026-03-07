import { api } from "./client";
import { unwrap } from "./types";
import type { ApiResponse, PageResponse } from "./types";
import type {
  AuditEntry,
  BusinessKPI,
  OrderDailyStats,
  ServiceHealthStatus,
  AlertItem,
} from "../types/models";
import { ServiceStatus, AlertSeverity } from "../types/enums";

// ── Query param shapes ───────────────────────────────────────────────────────

export interface DailyStatsParams {
  restaurantId?: string;
  from?: string; // ISO date e.g. "2025-01-01"
  to?: string;
}

export interface AuditFeedParams {
  page?: number;
  size?: number;
  resourceType?: string;
  actorRole?: string;
}

// ── Admin API ────────────────────────────────────────────────────────────────

export const adminApi = {
  /**
   * Aggregate KPIs — composed from order-service V2 stats endpoint.
   * Backend: GET /api/v2/orders/queries/stats/today
   */
  getBusinessKPIs: () =>
    api
      .get("api/v2/orders/queries/stats/today")
      .json<ApiResponse<BusinessKPI>>()
      .then(unwrap),

  /**
   * Daily order/revenue stats.
   * Backend: GET /api/v2/orders/queries/stats/daily?from=&to=&restaurantId=
   */
  getDailyStats: (params: DailyStatsParams = {}) =>
    api
      .get("api/v2/orders/queries/stats/daily", {
        searchParams: Object.fromEntries(
          Object.entries(params).filter(([, v]) => v !== undefined && v !== ""),
        ),
      })
      .json<ApiResponse<OrderDailyStats[]>>()
      .then(unwrap),

  /**
   * Service health — derived from Spring Boot Actuator health endpoints
   * exposed through the API gateway.
   * Backend: GET /api/v1/admin/health (aggregated by api-gateway)
   */
  getServiceHealth: () =>
    api
      .get("api/v1/admin/health")
      .json<ApiResponse<ServiceHealthStatus[]>>()
      .catch((err: unknown) => {
        // In dev mode return UNKNOWN stubs so the dashboard renders without a live backend.
        // In production, surface the real error so ops can detect the outage.
        if (!import.meta.env.DEV) throw err;
        const services = [
          "order-service",
          "payment-service",
          "kitchen-service",
          "delivery-service",
          "booking-service",
          "inventory-service",
          "catalog-service",
          "notification-service",
        ];
        return {
          success: true,
          data: services.map((s) => ({
            serviceName: s,
            status: ServiceStatus.UNKNOWN,
            uptimePercent: 0,
            p95LatencyMs: 0,
            errorRatePercent: 0,
            lastHeartbeat: new Date().toISOString(),
            instanceCount: 0,
          })),
          message: "Health endpoint unavailable — showing cached state",
          error: null,
          timestamp: new Date().toISOString(),
          traceId: null,
        } satisfies ApiResponse<ServiceHealthStatus[]>;
      })
      .then((r) => (r as ApiResponse<ServiceHealthStatus[]>).data),

  /**
   * Audit event feed — paginated.
   * Backend: GET /api/v1/admin/audit?page=&size=&resourceType=&actorRole=
   */
  getAuditFeed: (params: AuditFeedParams = {}) =>
    api
      .get("api/v1/admin/audit", {
        searchParams: Object.fromEntries(
          Object.entries({ page: 0, size: 20, ...params }).filter(
            ([, v]) => v !== undefined,
          ),
        ),
      })
      .json<ApiResponse<PageResponse<AuditEntry>>>()
      .then(unwrap),

  /**
   * Active (unacknowledged) system alerts.
   * Backend: GET /api/v1/admin/alerts?acknowledged=false
   */
  getActiveAlerts: () =>
    api
      .get("api/v1/admin/alerts", { searchParams: { acknowledged: false } })
      .json<ApiResponse<AlertItem[]>>()
      .catch((err: unknown) => {
        // In dev mode return an empty list so the dashboard renders without a live backend.
        // In production, surface the real error so ops can detect endpoint absence.
        if (!import.meta.env.DEV) throw err;
        return {
          success: true,
          data: [] as AlertItem[],
          message: "No active alerts",
          error: null,
          timestamp: new Date().toISOString(),
          traceId: null,
        } satisfies ApiResponse<AlertItem[]>;
      })
      .then((r) => (r as ApiResponse<AlertItem[]>).data),

  /**
   * Acknowledge a specific alert.
   * Backend: POST /api/v1/admin/alerts/{id}/acknowledge
   */
  acknowledgeAlert: (id: string) =>
    api
      .post(`api/v1/admin/alerts/${id}/acknowledge`)
      .json<ApiResponse<AlertItem>>()
      .then(unwrap),
};

// ── Re-export severity colours helper ────────────────────────────────────────

export const SEVERITY_COLOR: Record<AlertSeverity, string> = {
  [AlertSeverity.LOW]: "text-blue-600",
  [AlertSeverity.MEDIUM]: "text-yellow-600",
  [AlertSeverity.HIGH]: "text-orange-600",
  [AlertSeverity.CRITICAL]: "text-red-600",
};

export const SERVICE_STATUS_COLOR: Record<ServiceStatus, string> = {
  [ServiceStatus.HEALTHY]: "text-green-600",
  [ServiceStatus.DEGRADED]: "text-yellow-600",
  [ServiceStatus.DOWN]: "text-red-600",
  [ServiceStatus.UNKNOWN]: "text-gray-400",
};
