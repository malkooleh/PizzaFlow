import { AlertTriangle, CheckCircle2, Loader2, Bell } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useAcknowledgeAlert, useActiveAlerts } from "@/hooks/use-admin";
import { formatRelativeTime } from "@/lib/format";
import { AlertSeverity } from "@/types/enums";
import type { AlertItem } from "@/types/models";

const SEVERITY_VARIANT: Record<
  AlertSeverity,
  "secondary" | "info" | "warning" | "destructive"
> = {
  [AlertSeverity.LOW]: "secondary",
  [AlertSeverity.MEDIUM]: "info",
  [AlertSeverity.HIGH]: "warning",
  [AlertSeverity.CRITICAL]: "destructive",
};

function AlertCard({ alert }: { alert: AlertItem }) {
  const ack = useAcknowledgeAlert();

  return (
    <Card className={alert.isAcknowledged ? "opacity-60" : undefined}>
      <CardContent className="flex items-start gap-3 p-3">
        <AlertTriangle
          className={`mt-0.5 h-4 w-4 shrink-0 ${
            alert.severity === AlertSeverity.CRITICAL
              ? "text-red-500"
              : alert.severity === AlertSeverity.HIGH
                ? "text-orange-500"
                : "text-yellow-500"
          }`}
        />
        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <p className="text-sm font-medium leading-snug">{alert.title}</p>
            <Badge
              variant={SEVERITY_VARIANT[alert.severity]}
              className="shrink-0 text-xs"
            >
              {alert.severity}
            </Badge>
          </div>
          <p className="mt-0.5 text-xs text-muted-foreground">
            {alert.description}
          </p>
          <div className="mt-1.5 flex items-center justify-between">
            <span className="text-xs text-muted-foreground">
              {alert.serviceName} · {formatRelativeTime(alert.createdAt)}
            </span>
            {!alert.isAcknowledged && (
              <Button
                size="sm"
                variant="ghost"
                className="h-6 text-xs"
                disabled={ack.isPending}
                onClick={() => ack.mutate(alert.id)}
              >
                {ack.isPending ? (
                  <Loader2 className="mr-1 h-3 w-3 animate-spin" />
                ) : (
                  <CheckCircle2 className="mr-1 h-3 w-3" />
                )}
                Acknowledge
              </Button>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

export function AlertCenter() {
  const { data: alerts, isLoading } = useActiveAlerts();

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 3 }).map((_, i) => (
          <Skeleton key={i} className="h-20 w-full" />
        ))}
      </div>
    );
  }

  if (!alerts?.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center text-muted-foreground">
        <Bell className="mb-3 h-10 w-10 opacity-30" />
        <p className="font-medium">No active alerts</p>
        <p className="text-sm">All systems are operating normally.</p>
      </div>
    );
  }

  const sorted = [...alerts].sort((a, b) => {
    const order = [
      AlertSeverity.CRITICAL,
      AlertSeverity.HIGH,
      AlertSeverity.MEDIUM,
      AlertSeverity.LOW,
    ];
    const ai = order.indexOf(a.severity);
    const bi = order.indexOf(b.severity);
    return ai - bi;
  });

  return (
    <div className="space-y-2">
      {sorted.map((alert) => (
        <AlertCard key={alert.id} alert={alert} />
      ))}
    </div>
  );
}
