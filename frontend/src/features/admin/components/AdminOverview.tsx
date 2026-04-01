import {
  ShoppingBag,
  DollarSign,
  Truck,
  CalendarCheck,
  TrendingUp,
  CheckCircle,
  AlertTriangle,
  XCircle,
  HelpCircle,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useBusinessKPIs, useServiceHealth, useActiveAlerts } from "@/hooks/use-admin";
import { formatCurrency } from "@/lib/format";
import { ServiceStatus, AlertSeverity } from "@/types/enums";

const StatusIcon = {
  [ServiceStatus.HEALTHY]: <CheckCircle className="h-4 w-4 text-green-500" />,
  [ServiceStatus.DEGRADED]: <AlertTriangle className="h-4 w-4 text-yellow-500" />,
  [ServiceStatus.DOWN]: <XCircle className="h-4 w-4 text-red-500" />,
  [ServiceStatus.UNKNOWN]: <HelpCircle className="h-4 w-4 text-gray-400" />,
};

interface KPICardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ElementType;
  loading?: boolean;
}

function KPICard({ title, value, subtitle, icon: Icon, loading }: KPICardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
        <Icon className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        {loading ? (
          <Skeleton className="h-7 w-24" />
        ) : (
          <>
            <p className="text-2xl font-bold">{value}</p>
            {subtitle && (
              <p className="mt-1 text-xs text-muted-foreground">{subtitle}</p>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}

export function AdminOverview() {
  const { data: kpis, isLoading: kpisLoading } = useBusinessKPIs();
  const { data: health } = useServiceHealth();
  const { data: alerts } = useActiveAlerts();

  const downCount = health?.filter((s) => s.status === ServiceStatus.DOWN).length ?? 0;
  const degradedCount =
    health?.filter((s) => s.status === ServiceStatus.DEGRADED).length ?? 0;

  const criticalAlerts =
    alerts?.filter((a) => a.severity === AlertSeverity.CRITICAL && !a.isAcknowledged)
      .length ?? 0;

  return (
    <div className="space-y-6">
      {criticalAlerts > 0 && (
        <div className="flex items-center gap-2 rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-800 dark:bg-red-950 dark:text-red-300">
          <AlertTriangle className="h-4 w-4" />
          <strong>{criticalAlerts}</strong> critical alert
          {criticalAlerts !== 1 ? "s" : ""} require immediate attention.
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <KPICard
          title="Orders Today"
          value={kpis?.totalOrdersToday ?? "—"}
          subtitle="placed since midnight"
          icon={ShoppingBag}
          loading={kpisLoading}
        />
        <KPICard
          title="Revenue Today"
          value={kpis ? formatCurrency(kpis.revenueToday) : "—"}
          subtitle={
            kpis ? `avg ${formatCurrency(kpis.averageOrderValue)} / order` : undefined
          }
          icon={DollarSign}
          loading={kpisLoading}
        />
        <KPICard
          title="Active Deliveries"
          value={kpis?.activeDeliveries ?? "—"}
          subtitle="in transit right now"
          icon={Truck}
          loading={kpisLoading}
        />
        <KPICard
          title="Pending Bookings"
          value={kpis?.pendingBookings ?? "—"}
          subtitle="awaiting confirmation"
          icon={CalendarCheck}
          loading={kpisLoading}
        />
        <KPICard
          title="Payment Success Rate"
          value={kpis ? `${(kpis.paymentSuccessRate * 100).toFixed(1)}%` : "—"}
          subtitle="last 24 hours"
          icon={TrendingUp}
          loading={kpisLoading}
        />

        {/* Service health summary card */}
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Service Health
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-1 text-sm">
                {StatusIcon[ServiceStatus.HEALTHY]}
                <span>{(health?.filter((s) => s.status === ServiceStatus.HEALTHY).length ?? 0)}</span>
              </div>
              {degradedCount > 0 && (
                <div className="flex items-center gap-1 text-sm">
                  {StatusIcon[ServiceStatus.DEGRADED]}
                  <span>{degradedCount}</span>
                </div>
              )}
              {downCount > 0 && (
                <div className="flex items-center gap-1 text-sm">
                  {StatusIcon[ServiceStatus.DOWN]}
                  <span>{downCount}</span>
                </div>
              )}
            </div>
            <p className="mt-1 text-xs text-muted-foreground">
              {health?.length ?? 0} services monitored
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
