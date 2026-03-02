import {
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import { useDailyStats } from "@/hooks/use-admin";
import { Skeleton } from "@/components/ui/skeleton";
import { AlertTriangle } from "lucide-react";

interface PlatformAnalyticsProps {
  from?: string;
  to?: string;
  restaurantId?: string;
}

const CURRENCY = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
  maximumFractionDigits: 0,
});

/**
 * Admin-level platform analytics charts.
 *  – Orders & Revenue trend (area chart)
 *  – Order count distribution by day (bar chart)
 * Data sourced from order-service V2 daily stats endpoint.
 */
export function PlatformAnalytics({ from, to }: PlatformAnalyticsProps) {
  const { data = [], isLoading, isError } = useDailyStats({ from, to });

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-52 w-full rounded-lg" />
        <Skeleton className="h-52 w-full rounded-lg" />
      </div>
    );
  }

  if (isError || data.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 gap-2 text-muted-foreground">
        <AlertTriangle className="h-8 w-8 opacity-40" />
        <p className="text-sm">No analytics data available for the selected range.</p>
      </div>
    );
  }

  const chartData = data.map((d) => ({
    date: d.date,
    orders: d.totalOrders,
    revenue: d.totalRevenue,
    avgOrder: d.totalOrders > 0 ? Math.round(d.totalRevenue / d.totalOrders) : 0,
    cancelled: d.cancelledOrders ?? 0,
  }));

  return (
    <div className="space-y-8">
      {/* Revenue trend */}
      <div>
        <h3 className="text-sm font-semibold mb-3 text-muted-foreground uppercase tracking-wide">
          Revenue Trend
        </h3>
        <ResponsiveContainer width="100%" height={200}>
          <AreaChart data={chartData}>
            <defs>
              <linearGradient id="revenueGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
            <XAxis dataKey="date" tick={{ fontSize: 11 }} />
            <YAxis tickFormatter={(v) => CURRENCY.format(v as number)} tick={{ fontSize: 11 }} />
            <Tooltip formatter={(v) => CURRENCY.format(v as number)} />
            <Area
              type="monotone"
              dataKey="revenue"
              stroke="#6366f1"
              fill="url(#revenueGrad)"
              strokeWidth={2}
              name="Revenue"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* Orders vs Cancellations bar chart */}
      <div>
        <h3 className="text-sm font-semibold mb-3 text-muted-foreground uppercase tracking-wide">
          Orders vs Cancellations
        </h3>
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
            <XAxis dataKey="date" tick={{ fontSize: 11 }} />
            <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
            <Tooltip />
            <Legend wrapperStyle={{ fontSize: 12 }} />
            <Bar dataKey="orders" fill="#22c55e" name="Orders" radius={[3, 3, 0, 0]} />
            <Bar dataKey="cancelled" fill="#ef4444" name="Cancelled" radius={[3, 3, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Avg order value trend */}
      <div>
        <h3 className="text-sm font-semibold mb-3 text-muted-foreground uppercase tracking-wide">
          Avg Order Value
        </h3>
        <ResponsiveContainer width="100%" height={160}>
          <AreaChart data={chartData}>
            <defs>
              <linearGradient id="avgGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#f59e0b" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
            <XAxis dataKey="date" tick={{ fontSize: 11 }} />
            <YAxis tickFormatter={(v) => `$${v}`} tick={{ fontSize: 11 }} />
            <Tooltip formatter={(v) => `$${v}`} />
            <Area
              type="monotone"
              dataKey="avgOrder"
              stroke="#f59e0b"
              fill="url(#avgGrad)"
              strokeWidth={2}
              name="Avg Order ($)"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
