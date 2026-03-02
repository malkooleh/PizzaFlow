import { ChefHat, CheckCircle, Clock, Inbox, LayoutList } from "lucide-react";
import type { QueueStatusDTO } from "@/types/models";

interface QueueStatsProps {
  stats: QueueStatusDTO;
}

/**
 * Summary bar shown at the top of the KDS board.
 * Displays order counts by status and the current average wait time.
 */
export function QueueStats({ stats }: QueueStatsProps) {
  return (
    <div
      className="grid grid-cols-2 sm:grid-cols-5 gap-2"
      aria-label="Queue statistics"
    >
      <StatCard
        icon={<LayoutList className="h-4 w-4" />}
        label="Total"
        value={stats.totalOrders}
      />
      <StatCard
        icon={<Inbox className="h-4 w-4" />}
        label="Received"
        value={stats.receivedCount}
        valueClass="text-slate-700"
      />
      <StatCard
        icon={<ChefHat className="h-4 w-4" />}
        label="Preparing"
        value={stats.preparingCount}
        valueClass="text-blue-700"
      />
      <StatCard
        icon={<CheckCircle className="h-4 w-4" />}
        label="Ready"
        value={stats.readyCount}
        valueClass="text-green-700"
      />
      <StatCard
        icon={<Clock className="h-4 w-4" />}
        label="Avg Wait"
        value={`${stats.averageWaitTimeMinutes}m`}
      />
    </div>
  );
}

// ── StatCard helper ───────────────────────────────────────────────────────────

interface StatCardProps {
  icon: React.ReactNode;
  label: string;
  value: number | string;
  valueClass?: string;
}

function StatCard({
  icon,
  label,
  value,
  valueClass = "text-foreground",
}: StatCardProps) {
  return (
    <div className="flex items-center gap-2 rounded-lg bg-background border px-3 py-2">
      <span className="text-muted-foreground shrink-0">{icon}</span>
      <div className="min-w-0">
        <p className={`text-lg font-bold leading-none tabular-nums ${valueClass}`}>
          {value}
        </p>
        <p className="text-xs text-muted-foreground mt-0.5">{label}</p>
      </div>
    </div>
  );
}
