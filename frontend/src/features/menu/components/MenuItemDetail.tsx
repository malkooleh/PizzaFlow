import { useState } from "react";
import { useNavigate } from "@tanstack/react-router";
import { ChevronLeft, Clock, Flame, Leaf, Wheat, ShoppingCart, Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { PriceTag } from "@/components/common/PriceTag";
import { Skeleton } from "@/components/ui/skeleton";
import type { MenuItem, MenuItemModifier } from "@/types/models";
import { cn } from "@/lib/utils";

interface MenuItemDetailProps {
  item: MenuItem | undefined;
  isLoading: boolean;
  onAddToCart: (item: MenuItem, selectedModifiers: MenuItemModifier[]) => void;
}

export function MenuItemDetail({ item, isLoading, onAddToCart }: MenuItemDetailProps) {
  const navigate = useNavigate();

  // Track selected modifier IDs keyed by group name
  const [selections, setSelections] = useState<Record<string, string[]>>({});
  const [added, setAdded] = useState(false);

  if (isLoading) {
    return (
      <div className="space-y-4 max-w-2xl mx-auto">
        <Skeleton className="aspect-video w-full rounded-xl" />
        <Skeleton className="h-6 w-2/3" />
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-3/4" />
      </div>
    );
  }

  if (!item) {
    return (
      <div className="text-center py-16 text-muted-foreground">
        Item not found.
      </div>
    );
  }

  const toggleModifier = (groupName: string, modifierId: string, maxSelections: number) => {
    setSelections((prev) => {
      const current = prev[groupName] ?? [];
      if (current.includes(modifierId)) {
        return { ...prev, [groupName]: current.filter((id) => id !== modifierId) };
      }
      if (current.length >= maxSelections) {
        // Replace last selection for single-select groups
        const next = maxSelections === 1 ? [modifierId] : [...current.slice(0, maxSelections - 1), modifierId];
        return { ...prev, [groupName]: next };
      }
      return { ...prev, [groupName]: [...current, modifierId] };
    });
  };

  // Flatten selected modifiers
  const selectedModifiers: MenuItemModifier[] = item.modifierGroups.flatMap((g) =>
    g.options.filter((o) => (selections[g.name] ?? []).includes(o.modifierId))
  );

  const modifierTotal = selectedModifiers.reduce((sum, m) => sum + m.additionalPrice, 0);

  const handleAddToCart = () => {
    onAddToCart(item, selectedModifiers);
    setAdded(true);
    setTimeout(() => setAdded(false), 1500);
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Back button */}
      <Button variant="ghost" size="sm" onClick={() => void navigate({ to: "/menu" })}>
        <ChevronLeft className="h-4 w-4" />
        Back to Menu
      </Button>

      {/* Hero image */}
      <div className="aspect-video overflow-hidden rounded-xl bg-muted">
        {item.imageUrl ? (
          <img src={item.imageUrl} alt={item.name} className="h-full w-full object-cover" />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-7xl">🍕</div>
        )}
      </div>

      {/* Header */}
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">{item.name}</h1>
          <p className="mt-1 text-muted-foreground">{item.description}</p>
        </div>
        <PriceTag amount={item.price + modifierTotal} size="lg" className="shrink-0" />
      </div>

      {/* Tags */}
      <div className="flex flex-wrap gap-2">
        {item.isVegetarian && (
          <Badge variant="outline" className="gap-1">
            <Leaf className="h-3.5 w-3.5 text-green-500" /> Vegetarian
          </Badge>
        )}
        {item.isVegan && (
          <Badge variant="outline" className="gap-1">
            <Leaf className="h-3.5 w-3.5 text-emerald-600" /> Vegan
          </Badge>
        )}
        {item.isGlutenFree && (
          <Badge variant="outline" className="gap-1">
            <Wheat className="h-3.5 w-3.5 text-amber-500" /> Gluten-Free
          </Badge>
        )}
        {item.calories && (
          <Badge variant="outline" className="gap-1">
            <Flame className="h-3.5 w-3.5 text-orange-500" /> {item.calories} kcal
          </Badge>
        )}
        <Badge variant="outline" className="gap-1">
          <Clock className="h-3.5 w-3.5" /> ~{item.preparationTimeMinutes} min
        </Badge>
      </div>

      {/* Ingredients */}
      {item.ingredients.length > 0 && (
        <div>
          <p className="text-sm font-medium mb-1">Ingredients</p>
          <p className="text-sm text-muted-foreground">{item.ingredients.join(", ")}</p>
        </div>
      )}

      {/* Allergens */}
      {item.allergens.length > 0 && (
        <div>
          <p className="text-sm font-medium mb-1 text-destructive">Allergens</p>
          <p className="text-sm text-muted-foreground">{item.allergens.join(", ")}</p>
        </div>
      )}

      {/* Modifier groups */}
      {item.modifierGroups.map((group) => (
        <div key={group.name}>
          <Separator />
          <div className="py-4 space-y-3">
            <div className="flex items-center justify-between">
              <p className="font-medium">{group.name}</p>
              {group.required && (
                <Badge variant="secondary" className="text-xs">Required</Badge>
              )}
            </div>
            <div className="grid grid-cols-2 gap-2">
              {group.options.map((opt) => {
                const selected = (selections[group.name] ?? []).includes(opt.modifierId);
                return (
                  <button
                    key={opt.modifierId}
                    onClick={() => toggleModifier(group.name, opt.modifierId, group.maxSelections)}
                    className={cn(
                      "flex items-center justify-between rounded-lg border px-3 py-2 text-sm transition-colors",
                      "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
                      selected
                        ? "border-primary bg-primary/10 font-medium"
                        : "border-border hover:bg-accent"
                    )}
                    aria-pressed={selected}
                  >
                    <span>{opt.name}</span>
                    {opt.additionalPrice > 0 && (
                      <span className="text-muted-foreground">+${opt.additionalPrice.toFixed(2)}</span>
                    )}
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      ))}

      {/* CTA */}
      <Button
        size="lg"
        className="w-full"
        disabled={!item.isAvailable}
        onClick={handleAddToCart}
      >
        {added ? (
          <>
            <Check className="h-5 w-5" />
            Added!
          </>
        ) : (
          <>
            <ShoppingCart className="h-5 w-5" />
            Add to Cart — <PriceTag amount={item.price + modifierTotal} className="text-primary-foreground" />
          </>
        )}
      </Button>
    </div>
  );
}
