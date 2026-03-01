import { MenuItemCard } from "./MenuItemCard";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/feedback/EmptyState";
import { UtensilsCrossed } from "lucide-react";
import type { MenuItem } from "@/types/models";

interface MenuGridProps {
  items: MenuItem[] | undefined;
  isLoading: boolean;
  onAddToCart: (item: MenuItem) => void;
}

function MenuGridSkeleton() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      {Array.from({ length: 8 }).map((_, i) => (
        <div key={i} className="space-y-2">
          <Skeleton className="aspect-video w-full rounded-xl" />
          <Skeleton className="h-4 w-3/4" />
          <Skeleton className="h-3 w-1/2" />
          <Skeleton className="h-8 w-full" />
        </div>
      ))}
    </div>
  );
}

export function MenuGrid({ items, isLoading, onAddToCart }: MenuGridProps) {
  if (isLoading) return <MenuGridSkeleton />;

  if (!items || items.length === 0) {
    return (
      <EmptyState
        icon={<UtensilsCrossed className="h-12 w-12" />}
        title="No items found"
        description="Try a different category or clear your search."
      />
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      {items.map((item) => (
        <MenuItemCard key={item.id} item={item} onAddToCart={onAddToCart} />
      ))}
    </div>
  );
}
