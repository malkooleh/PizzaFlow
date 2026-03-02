import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/api/admin.api";
import type { DailyStatsParams, AuditFeedParams } from "@/api/admin.api";

// ── Query keys ───────────────────────────────────────────────────────────────

export const adminKeys = {
  kpis: ["admin", "kpis"] as const,
  health: ["admin", "health"] as const,
  dailyStats: (params: DailyStatsParams) =>
    ["admin", "stats", "daily", params] as const,
  audit: (params: AuditFeedParams) =>
    ["admin", "audit", params] as const,
  alerts: ["admin", "alerts"] as const,
};

// ── Hooks ────────────────────────────────────────────────────────────────────

/** Business KPIs — polls every 30 s. */
export function useBusinessKPIs() {
  return useQuery({
    queryKey: adminKeys.kpis,
    queryFn: () => adminApi.getBusinessKPIs(),
    staleTime: 15_000,
    refetchInterval: 30_000,
  });
}

/** Per-service health status — polls every 30 s. */
export function useServiceHealth() {
  return useQuery({
    queryKey: adminKeys.health,
    queryFn: () => adminApi.getServiceHealth(),
    staleTime: 20_000,
    refetchInterval: 30_000,
  });
}

/** Daily order/revenue stats for a date range. */
export function useDailyStats(params: DailyStatsParams = {}) {
  return useQuery({
    queryKey: adminKeys.dailyStats(params),
    queryFn: () => adminApi.getDailyStats(params),
    staleTime: 60_000,
  });
}

/**
 * Paginated audit event feed.
 * @param params page / size / resourceType / actorRole filters
 */
export function useAuditFeed(params: AuditFeedParams = {}) {
  return useQuery({
    queryKey: adminKeys.audit(params),
    queryFn: () => adminApi.getAuditFeed(params),
    staleTime: 30_000,
  });
}

/** Active (unacknowledged) system alerts — polls every 30 s. */
export function useActiveAlerts() {
  return useQuery({
    queryKey: adminKeys.alerts,
    queryFn: () => adminApi.getActiveAlerts(),
    staleTime: 20_000,
    refetchInterval: 30_000,
  });
}

/** Acknowledge a specific alert. */
export function useAcknowledgeAlert() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => adminApi.acknowledgeAlert(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: adminKeys.alerts });
    },
  });
}
