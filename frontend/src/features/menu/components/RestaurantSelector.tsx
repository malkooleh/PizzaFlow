import { useRestaurants } from "@/hooks/use-menu";
import { useUiStore } from "@/stores/ui.store";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { MapPin } from "lucide-react";
import { cn } from "@/lib/utils";

export function RestaurantSelector() {
  const { data: restaurants, isLoading, error } = useRestaurants();
  const { selectedRestaurantId, setSelectedRestaurant: setSelectedRestaurantId } = useUiStore();

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {Array.from({ length: 3 }).map((_, i) => (
          <Skeleton key={i} className="h-28 rounded-xl" />
        ))}
      </div>
    );
  }

  if (error || !restaurants?.length) {
    return (
      <p className="text-sm text-muted-foreground text-center py-8">
        No restaurants available at the moment.
      </p>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {restaurants.map((r) => (
        <Card
          key={r.id}
          role="button"
          tabIndex={0}
          aria-pressed={selectedRestaurantId === r.id}
          onClick={() => setSelectedRestaurantId(r.id)}
          onKeyDown={(e) => e.key === "Enter" && setSelectedRestaurantId(r.id)}
          className={cn(
            "cursor-pointer transition-all hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
            selectedRestaurantId === r.id
              ? "border-primary ring-1 ring-primary"
              : "border-border"
          )}
        >
          <CardContent className="flex items-start gap-3 p-4">
            {r.imageUrl ? (
              <img
                src={r.imageUrl}
                alt={r.name}
                className="h-14 w-14 rounded-lg object-cover shrink-0"
              />
            ) : (
              <div className="flex h-14 w-14 items-center justify-center rounded-lg bg-muted text-2xl shrink-0">
                🍕
              </div>
            )}
            <div className="min-w-0">
              <p className="font-semibold truncate">{r.name}</p>
              <p className="flex items-center gap-1 text-xs text-muted-foreground mt-0.5 truncate">
                <MapPin className="h-3 w-3 shrink-0" aria-hidden="true" />
                {r.address}
              </p>
              <p className="text-xs text-muted-foreground mt-0.5">
                {r.openingTime} – {r.closingTime}
              </p>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
