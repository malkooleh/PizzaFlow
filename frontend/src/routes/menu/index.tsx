import { createFileRoute } from "@tanstack/react-router";
import { z } from "zod";
import { toast } from "sonner";
import { useMemo } from "react";
import { useUiStore } from "@/stores/ui.store";
import { useCartStore } from "@/stores/cart.store";
import { useMenu, useFeaturedItems, useSearchMenu, useMenuByCategory } from "@/hooks/use-menu";
import { RestaurantSelector } from "@/features/menu/components/RestaurantSelector";
import { CategoryTabs } from "@/features/menu/components/CategoryTabs";
import { DietaryFilter } from "@/features/menu/components/DietaryFilter";
import { MenuSearch } from "@/features/menu/components/MenuSearch";
import { MenuGrid } from "@/features/menu/components/MenuGrid";
import { MenuItemCard } from "@/features/menu/components/MenuItemCard";
import { MenuCategory } from "@/types/enums";
import type { DietaryKey } from "@/features/menu/components/DietaryFilter";
import type { MenuItem } from "@/types/models";

// URL search schema for filters — all optional, bookmarkable
const searchSchema = z.object({
  q: z.string().optional(),
  category: z.nativeEnum(MenuCategory).optional(),
  dietary: z.array(z.enum(["vegetarian", "vegan", "glutenFree"])).optional(),
});

export const Route = createFileRoute("/menu/")({
  validateSearch: (input) => searchSchema.parse(input),
  component: MenuPage,
});

function MenuPage() {
  const { q = "", category, dietary = [] } = Route.useSearch();
  const navigate = Route.useNavigate();

  const { selectedRestaurantId } = useUiStore();
  const addItem = useCartStore((s) => s.addItem);
  const wouldClearCart = useCartStore((s) => s.wouldClearCart);

  const hasSearch = q.trim().length >= 2;
  const hasCategory = !!category;

  const fullMenu = useMenu(!hasSearch && !hasCategory ? selectedRestaurantId : null);
  const byCategory = useMenuByCategory(
    hasCategory && !hasSearch ? selectedRestaurantId : null,
    hasCategory ? category : null
  );
  const searchResults = useSearchMenu(hasSearch ? selectedRestaurantId : null, q);
  const featured = useFeaturedItems(!selectedRestaurantId ? null : selectedRestaurantId);

  // Pick the active data source
  const { data: rawItems, isLoading } = hasSearch
    ? searchResults
    : hasCategory
    ? byCategory
    : fullMenu;

  // Client-side dietary filtering
  const items = useMemo((): MenuItem[] | undefined => {
    if (!rawItems) return undefined;
    return rawItems.filter((item) => {
      if ((dietary as DietaryKey[]).includes("vegetarian") && !item.isVegetarian) return false;
      if ((dietary as DietaryKey[]).includes("vegan") && !item.isVegan) return false;
      if ((dietary as DietaryKey[]).includes("glutenFree") && !item.isGlutenFree) return false;
      return true;
    });
  }, [rawItems, dietary]);

  const handleAddToCart = (item: MenuItem) => {
    // Warn if adding from different restaurant
    if (wouldClearCart(item.restaurantId)) {
      if (!window.confirm("Adding this item will clear your current cart. Continue?")) return;
    }
    addItem(item.restaurantId, {
      menuItemId: item.id,
      menuItemName: item.name,
      basePrice: item.price,
      quantity: 1,
      imageUrl: item.imageUrl,
      selectedModifiers: [],
    });
    toast.success(`${item.name} added to cart`);
  };

  const setQ = (next: string) => void navigate({ search: (prev) => ({ ...prev, q: next || undefined }) });
  const setCategory = (cat: MenuCategory | "ALL") =>
    void navigate({ search: (prev) => ({ ...prev, category: cat === "ALL" ? undefined : cat }) });
  const setDietary = (next: DietaryKey[]) =>
    void navigate({ search: (prev) => ({ ...prev, dietary: next.length ? next : undefined }) });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Menu</h1>
        <p className="text-muted-foreground text-sm">Select a restaurant and explore the menu</p>
      </div>

      {/* Restaurant selection */}
      <section aria-label="Select restaurant">
        <RestaurantSelector />
      </section>

      {/* Only show menu when a restaurant is selected */}
      {selectedRestaurantId ? (
        <>
          {/* Search & filters */}
          <div className="flex flex-col gap-3">
            <MenuSearch value={q} onChange={setQ} />
            <CategoryTabs value={category ?? "ALL"} onChange={setCategory} />
            <DietaryFilter active={dietary as DietaryKey[]} onChange={setDietary} />
          </div>

          {/* Featured strip — shown only when not filtering */}
          {!hasSearch && !hasCategory && !dietary.length && featured.data && featured.data.length > 0 && (
            <section aria-label="Featured items">
              <h2 className="text-lg font-semibold mb-3">⭐ Featured</h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {featured.data.slice(0, 3).map((item) => (
                  <MenuItemCard key={item.id} item={item} onAddToCart={handleAddToCart} />
                ))}
              </div>
            </section>
          )}

          {/* Main menu grid */}
          <MenuGrid items={items} isLoading={isLoading} onAddToCart={handleAddToCart} />
        </>
      ) : (
        <p className="text-center text-muted-foreground py-12">
          Select a restaurant above to view the menu.
        </p>
      )}
    </div>
  );
}
