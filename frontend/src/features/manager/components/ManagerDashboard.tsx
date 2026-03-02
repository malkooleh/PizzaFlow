import {
  ShoppingBag,
  DollarSign,
  CalendarDays,
  AlertTriangle,
  TrendingUp,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { formatCurrency } from "@/lib/format";
import { useStockLevels, useTodayBookings } from "@/hooks/use-manager";
import { useQuery } from "@tanstack/react-query";
import { ordersApi } from "@/api/orders.api";

interface ManagerDashboardProps {
  restaurantId: string;
}

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ElementType;
  iconClassName?: string;
  loading?: boolean;
}

function StatCard({
  title,
  value,
  subtitle,
  icon: Icon,
  iconClassName = "text-primary",
  loading,
}: StatCardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
        <Icon className={`h-4 w-4 ${iconClassName}`} />
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

export function ManagerDashboard({ restaurantId }: ManagerDashboardProps) {
  const bookings = useTodayBookings(restaurantId);
  const stock = useStockLevels(restaurantId);

  // Today's orders via order-service (uses restaurant numeric ID, coerce)
  const ordersQuery = useQuery({
    queryKey: ["manager", "orders-today", restaurantId],
    queryFn: async () => {
      // Fetch recent orders for revenue calculation
      // We use the stats endpoint if available, otherwise return null
      return null as null;
    },
    staleTime: 30_000,
  });

  const lowStockCount =
    stock.data?.filter((s) => s.currentStock < s.minStockLevel).length ?? 0;

  const confirmedBookings =
    bookings.data?.filter((b) =>
      ["CONFIRMED", "SEATED"].includes(b.status),
    ).length ?? 0;

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <StatCard
        title="Today's Bookings"
        value={bookings.isLoading ? "—" : (bookings.data?.length ?? 0)}
        subtitle={`${confirmedBookings} confirmed`}
        icon={CalendarDays}
        loading={bookings.isLoading}
      />
      <StatCard
        title="Menu Items"
        value={stock.isLoading ? "—" : (stock.data?.length ?? 0)}
        subtitle="tracked ingredients"
        icon={ShoppingBag}
        loading={stock.isLoading}
      />
      <StatCard
        title="Low Stock Alerts"
        value={lowStockCount}
        subtitle={lowStockCount > 0 ? "need restocking" : "all levels OK"}
        icon={AlertTriangle}
        iconClassName={lowStockCount > 0 ? "text-yellow-500" : "text-green-500"}
        loading={stock.isLoading}
      />
      <StatCard
        title="Revenue Today"
        value="—"
        subtitle="from order service"
        icon={DollarSign}
        loading={ordersQuery.isLoading}
      />
    </div>
  );
}
