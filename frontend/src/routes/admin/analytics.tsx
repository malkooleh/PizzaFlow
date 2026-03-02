import { useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import { Lock } from "lucide-react";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { hasRole } from "@/lib/auth";
import { UserRole } from "@/types/enums";
import { DateRangeFilter } from "@/features/admin/components/DateRangeFilter";
import { RestaurantFilter } from "@/features/admin/components/RestaurantFilter";
import { PlatformAnalytics } from "@/features/admin/components/PlatformAnalytics";
import { OpsTimeline } from "@/features/admin/components/OpsTimeline";

export const Route = createFileRoute("/admin/analytics")({
  component: AdminAnalyticsRoute,
});

const ALLOWED_ROLES = [UserRole.SYSTEM_ADMIN];

function AdminAnalyticsRoute() {
  const auth = useAuth();
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [restaurantId, setRestaurantId] = useState<string | undefined>();

  return (
    <ProtectedRoute>
      {!hasRole(auth, ALLOWED_ROLES) ? (
        <div className="flex flex-col items-center justify-center py-24 gap-3 text-center px-4">
          <Lock className="h-12 w-12 text-muted-foreground/40" />
          <h1 className="text-xl font-semibold">Access Restricted</h1>
          <p className="text-muted-foreground text-sm">
            The Admin Panel requires the System Admin role.
          </p>
        </div>
      ) : (
        <div className="p-4 space-y-8">
          <h1 className="text-xl font-semibold">Platform Analytics</h1>

          {/* Global filters */}
          <div className="flex flex-wrap gap-4 items-end">
            <DateRangeFilter
              from={from}
              to={to}
              onFromChange={setFrom}
              onToChange={setTo}
            />
            <RestaurantFilter value={restaurantId} onChange={setRestaurantId} />
          </div>

          {/* Cross-service charts */}
          <section>
            <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide mb-4">
              Platform Metrics
            </h2>
            <PlatformAnalytics
              from={from || undefined}
              to={to || undefined}
              restaurantId={restaurantId}
            />
          </section>

          {/* Operations timeline */}
          <section>
            <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide mb-4">
              Operations Timeline
            </h2>
            <OpsTimeline />
          </section>
        </div>
      )}
    </ProtectedRoute>
  );
}
