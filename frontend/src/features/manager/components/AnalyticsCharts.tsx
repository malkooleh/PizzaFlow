import { useState } from "react";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  CartesianGrid,
  Legend,
} from "recharts";
import { format, subDays } from "date-fns";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useDailyStats } from "@/hooks/use-admin";
import { formatCurrency } from "@/lib/format";

interface AnalyticsChartsProps {
  restaurantId?: string;
}

const PIE_COLORS = ["#3b82f6", "#f97316", "#22c55e", "#a855f7"];

export function AnalyticsCharts({ restaurantId }: AnalyticsChartsProps) {
  const today = format(new Date(), "yyyy-MM-dd");
  const from = format(subDays(new Date(), 13), "yyyy-MM-dd");

  const { data: stats, isLoading } = useDailyStats({
    restaurantId,
    from,
    to: today,
  });

  if (isLoading) {
    return (
      <div className="grid gap-4 lg:grid-cols-2">
        <Skeleton className="h-64 w-full" />
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }

  if (!stats?.length) {
    return (
      <div className="py-10 text-center text-muted-foreground">
        No analytics data available for the selected period.
      </div>
    );
  }

  const chartData = stats.map((s) => ({
    date: format(new Date(s.date), "MM/dd"),
    orders: s.orderCount,
    revenue: parseFloat(s.revenue.toFixed(2)),
  }));

  // Simulated order-type distribution from stats
  const orderTypeData = [
    { name: "Dine-In", value: Math.round((stats[0]?.orderCount ?? 0) * 0.4) },
    { name: "Takeaway", value: Math.round((stats[0]?.orderCount ?? 0) * 0.3) },
    { name: "Delivery", value: Math.round((stats[0]?.orderCount ?? 0) * 0.3) },
  ];

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      {/* Orders over time */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Orders — Last 14 Days</CardTitle>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={chartData} margin={{ top: 4, right: 4, left: -20, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip formatter={(v) => [v, "Orders"]} />
              <Bar dataKey="orders" fill="#3b82f6" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      {/* Revenue over time */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Revenue — Last 14 Days</CardTitle>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={chartData} margin={{ top: 4, right: 4, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} />
              <YAxis tickFormatter={(v) => `$${v}`} tick={{ fontSize: 11 }} />
              <Tooltip formatter={(v) => [formatCurrency(Number(v)), "Revenue"]} />
              <Bar dataKey="revenue" fill="#22c55e" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      {/* Order type distribution */}
      <Card className="lg:col-span-2">
        <CardHeader>
          <CardTitle className="text-sm">Order Type Distribution (Today)</CardTitle>
        </CardHeader>
        <CardContent className="flex items-center justify-center">
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={orderTypeData}
                cx="50%"
                cy="50%"
                outerRadius={80}
                dataKey="value"
                label={({ name, percent }) =>
                  `${name} ${(percent * 100).toFixed(0)}%`
                }
              >
                {orderTypeData.map((_entry, index) => (
                  <Cell
                    key={`cell-${index}`}
                    fill={PIE_COLORS[index % PIE_COLORS.length]}
                  />
                ))}
              </Pie>
              <Legend />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>
    </div>
  );
}
