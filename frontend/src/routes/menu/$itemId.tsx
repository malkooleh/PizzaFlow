import { createFileRoute } from "@tanstack/react-router";
import { toast } from "sonner";
import { useMenuItem } from "@/hooks/use-menu";
import { MenuItemDetail } from "@/features/menu/components/MenuItemDetail";
import { useCartStore } from "@/stores/cart.store";
import type { MenuItem, MenuItemModifier } from "@/types/models";

export const Route = createFileRoute("/menu/$itemId")({
  component: MenuItemPage,
});

function MenuItemPage() {
  const { itemId } = Route.useParams();
  const { data: item, isLoading } = useMenuItem(itemId);

  const addItem = useCartStore((s) => s.addItem);
  const wouldClearCart = useCartStore((s) => s.wouldClearCart);

  const handleAddToCart = (menuItem: MenuItem, selectedModifiers: MenuItemModifier[]) => {
    if (wouldClearCart(menuItem.restaurantId)) {
      if (!window.confirm("Adding this item will clear your current cart. Continue?")) return;
    }
    addItem(menuItem.restaurantId, {
      menuItemId: menuItem.id,
      menuItemName: menuItem.name,
      basePrice: menuItem.price,
      quantity: 1,
      imageUrl: menuItem.imageUrl,
      selectedModifiers: selectedModifiers.map((m) => ({
        modifierId: m.modifierId,
        name: m.name,
        additionalPrice: m.additionalPrice,
      })),
    });
    toast.success(`${menuItem.name} added to cart`);
  };

  return (
    <div className="py-4">
      <MenuItemDetail item={item} isLoading={isLoading} onAddToCart={handleAddToCart} />
    </div>
  );
}
