import { Link } from "@tanstack/react-router";
import { PlusCircle, Clock, Leaf, Wheat } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { PriceTag } from "@/components/common/PriceTag";
import type { MenuItem } from "@/types/models";
import { cn } from "@/lib/utils";

interface MenuItemCardProps {
  item: MenuItem;
  onAddToCart?: (item: MenuItem) => void;
  /** When true the card is displayed in a compact list view */
  compact?: boolean;
}

export function MenuItemCard({ item, onAddToCart, compact = false }: MenuItemCardProps) {
  const unavailable = !item.isAvailable;

  return (
    <Card
      className={cn(
        "group overflow-hidden transition-shadow hover:shadow-md",
        unavailable && "opacity-60"
      )}
    >
      {!compact && (
        <Link to="/menu/$itemId" params={{ itemId: item.id }}>
          <div className="relative aspect-video overflow-hidden bg-muted">
            {item.imageUrl ? (
              <img
                src={item.imageUrl}
                alt={item.name}
                className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                loading="lazy"
              />
            ) : (
              <div className="flex h-full w-full items-center justify-center text-5xl">🍕</div>
            )}
            {item.isFeatured && (
              <Badge className="absolute left-2 top-2" variant="default">
                Featured
              </Badge>
            )}
            {unavailable && (
              <div className="absolute inset-0 flex items-center justify-center bg-background/60">
                <span className="text-sm font-medium">Unavailable</span>
              </div>
            )}
          </div>
        </Link>
      )}

      <CardContent className={cn("flex flex-col gap-2", compact ? "p-3" : "p-4")}>
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0">
            <Link
              to="/menu/$itemId"
              params={{ itemId: item.id }}
              className="font-semibold leading-tight hover:text-primary transition-colors line-clamp-1"
            >
              {item.name}
            </Link>
            {!compact && (
              <p className="mt-0.5 text-sm text-muted-foreground line-clamp-2">
                {item.description}
              </p>
            )}
          </div>
          <PriceTag amount={item.price} className="shrink-0" />
        </div>

        {/* Dietary + prep-time tags */}
        <div className="flex flex-wrap items-center gap-1.5">
          {item.isVegetarian && (
            <Badge variant="outline" className="gap-1 text-xs">
              <Leaf className="h-3 w-3 text-green-500" aria-hidden="true" />
              Veg
            </Badge>
          )}
          {item.isVegan && (
            <Badge variant="outline" className="gap-1 text-xs">
              <Leaf className="h-3 w-3 text-emerald-600" aria-hidden="true" />
              Vegan
            </Badge>
          )}
          {item.isGlutenFree && (
            <Badge variant="outline" className="gap-1 text-xs">
              <Wheat className="h-3 w-3 text-amber-500" aria-hidden="true" />
              GF
            </Badge>
          )}
          <span className="ml-auto flex items-center gap-1 text-xs text-muted-foreground">
            <Clock className="h-3 w-3" aria-hidden="true" />
            ~{item.preparationTimeMinutes} min
          </span>
        </div>

        {/* Add to cart */}
        {onAddToCart && (
          <Button
            size="sm"
            className="w-full mt-1"
            disabled={unavailable}
            onClick={() => onAddToCart(item)}
            aria-label={`Add ${item.name} to cart`}
          >
            <PlusCircle className="h-4 w-4" />
            Add to Cart
          </Button>
        )}
      </CardContent>
    </Card>
  );
}
