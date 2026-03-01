import { useMemo, useState } from "react";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Skeleton } from "@/components/ui/skeleton";
import { ShoppingBag } from "lucide-react";
import { OrderStatus } from "@/types/enums";
import type { OrderResponse } from "@/types/models";
import { useOrders, ACTIVE_ORDER_STATUSES } from "@/hooks/use-orders";
import { OrderCard } from "./OrderCard";

type FilterTab = "all" | "active" | "completed" | "cancelled";

const COMPLETED_STATUSES = new Set([OrderStatus.DELIVERED, OrderStatus.COMPLETED]);

function filterOrders(orders: OrderResponse[], tab: FilterTab): OrderResponse[] {
  switch (tab) {
    case "active":
      return orders.filter((o) => ACTIVE_ORDER_STATUSES.has(o.status));
    case "completed":
      return orders.filter((o) => COMPLETED_STATUSES.has(o.status));
    case "cancelled":
      return orders.filter((o) => o.status === OrderStatus.CANCELLED);
    default:
      return orders;
  }
}

interface OrderListProps {
  customerId: number;
}

export function OrderList({ customerId }: OrderListProps) {
  const [activeTab, setActiveTab] = useState<FilterTab>("all");
  const { data: orders = [], isLoading, error } = useOrders(customerId);

  const sorted = useMemo(
    () => [...orders].sort((a, b) => b.createdAt.localeCompare(a.createdAt)),
    [orders],
  );

  const filtered = useMemo(() => filterOrders(sorted, activeTab), [sorted, activeTab]);

  const counts = useMemo(
    () => ({
      active: sorted.filter((o) => ACTIVE_ORDER_STATUSES.has(o.status)).length,
    }),
    [sorted],
  );

  if (isLoading) {
    return <OrderListSkeleton />;
  }

  if (error) {
    return (
      <div className="rounded-lg border border-destructive/30 bg-destructive/5 p-6 text-center">
        <p className="text-destructive font-medium">Failed to load orders.</p>
        <p className="text-sm text-muted-foreground mt-1">Please refresh the page to try again.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as FilterTab)}>
        <TabsList>
          <TabsTrigger value="all">
            All
            {sorted.length > 0 && (
              <span className="ml-1.5 text-xs text-muted-foreground">({sorted.length})</span>
            )}
          </TabsTrigger>
          <TabsTrigger value="active">
            Active
            {counts.active > 0 && (
              <span className="ml-1.5 inline-flex h-4 w-4 items-center justify-center rounded-full bg-primary text-[10px] text-primary-foreground">
                {counts.active}
              </span>
            )}
          </TabsTrigger>
          <TabsTrigger value="completed">Completed</TabsTrigger>
          <TabsTrigger value="cancelled">Cancelled</TabsTrigger>
        </TabsList>
      </Tabs>

      {filtered.length === 0 ? (
        <EmptyOrderState tab={activeTab} />
      ) : (
        <div className="space-y-3">
          {filtered.map((order) => (
            <OrderCard key={order.id} order={order} />
          ))}
        </div>
      )}
    </div>
  );
}

function EmptyOrderState({ tab }: { tab: FilterTab }) {
  const messages: Record<FilterTab, { title: string; description: string }> = {
    all: {
      title: "No orders yet",
      description: "Your order history will appear here after your first purchase.",
    },
    active: {
      title: "No active orders",
      description: "You don't have any orders in progress right now.",
    },
    completed: {
      title: "No completed orders",
      description: "Orders that have been delivered or completed will show here.",
    },
    cancelled: {
      title: "No cancelled orders",
      description: "You haven't cancelled any orders.",
    },
  };

  const { title, description } = messages[tab];

  return (
    <div className="flex flex-col items-center justify-center py-16 gap-3 text-center">
      <ShoppingBag className="h-12 w-12 text-muted-foreground/30" />
      <div>
        <p className="font-semibold text-foreground">{title}</p>
        <p className="text-sm text-muted-foreground mt-1 max-w-xs">{description}</p>
      </div>
    </div>
  );
}

function OrderListSkeleton() {
  return (
    <div className="space-y-4">
      <div className="flex gap-2">
        {[1, 2, 3, 4].map((n) => (
          <Skeleton key={n} className="h-9 w-24 rounded-md" />
        ))}
      </div>
      <div className="space-y-3">
        {[1, 2, 3].map((n) => (
          <Skeleton key={n} className="h-20 w-full rounded-lg" />
        ))}
      </div>
    </div>
  );
}
