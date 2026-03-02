import {
  CheckCircle,
  AlertTriangle,
  XCircle,
  HelpCircle,
  Activity,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { useServiceHealth } from "@/hooks/use-admin";
import { ServiceStatus } from "@/types/enums";
import { formatRelativeTime } from "@/lib/format";
import type { ServiceHealthStatus } from "@/types/models";

const STATUS_CONFIG: Record<
  ServiceStatus,
  {
    icon: React.ReactElement;
    badge: "success" | "warning" | "destructive" | "secondary";
    label: string;
  }
> = {
  [ServiceStatus.HEALTHY]: {
    icon: <CheckCircle className="h-4 w-4 text-green-500" />,
    badge: "success",
    label: "Healthy",
  },
  [ServiceStatus.DEGRADED]: {
    icon: <AlertTriangle className="h-4 w-4 text-yellow-500" />,
    badge: "warning",
    label: "Degraded",
  },
  [ServiceStatus.DOWN]: {
    icon: <XCircle className="h-4 w-4 text-red-500" />,
    badge: "destructive",
    label: "Down",
  },
  [ServiceStatus.UNKNOWN]: {
    icon: <HelpCircle className="h-4 w-4 text-gray-400" />,
    badge: "secondary",
    label: "Unknown",
  },
};

function ServiceCard({ service }: { service: ServiceHealthStatus }) {
  const config = STATUS_CONFIG[service.status];

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium">
          {service.serviceName.replace("-service", "")}
        </CardTitle>
        <div className="flex items-center gap-1">
          {config.icon}
          <Badge variant={config.badge} className="text-xs">
            {config.label}
          </Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-1 text-xs text-muted-foreground">
        {service.status !== ServiceStatus.UNKNOWN && (
          <>
            <div className="flex justify-between">
              <span>Uptime</span>
              <span className="font-mono">{service.uptimePercent.toFixed(2)}%</span>
            </div>
            <div className="flex justify-between">
              <span>P95 latency</span>
              <span className="font-mono">{service.p95LatencyMs} ms</span>
            </div>
            <div className="flex justify-between">
              <span>Error rate</span>
              <span
                className={`font-mono ${service.errorRatePercent > 5 ? "text-destructive" : ""}`}
              >
                {service.errorRatePercent.toFixed(2)}%
              </span>
            </div>
            <div className="flex justify-between">
              <span>Instances</span>
              <span className="font-mono">{service.instanceCount}</span>
            </div>
          </>
        )}
        <div className="flex items-center justify-between pt-1">
          <span className="flex items-center gap-1">
            <Activity className="h-3 w-3" />
            Last seen
          </span>
          <span>{formatRelativeTime(service.lastHeartbeat)}</span>
        </div>
      </CardContent>
    </Card>
  );
}

export function ServiceHealthGrid() {
  const { data: services, isLoading } = useServiceHealth();

  if (isLoading) {
    return (
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {Array.from({ length: 8 }).map((_, i) => (
          <Skeleton key={i} className="h-36 w-full" />
        ))}
      </div>
    );
  }

  if (!services?.length) {
    return (
      <div className="py-10 text-center text-muted-foreground">
        No service health data available.
      </div>
    );
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {services.map((s) => (
        <ServiceCard key={s.serviceName} service={s} />
      ))}
    </div>
  );
}
