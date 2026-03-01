import { Minus, Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { formatCurrency } from "@/lib/format";
import { useCartStore, type CartItem } from "@/stores/cart.store";

interface CartItemRowProps {
  item: CartItem;
}

export function CartItemRow({ item }: CartItemRowProps) {
  const { updateQuantity, removeItem } = useCartStore();

  const lineTotal =
    (item.basePrice + item.selectedModifiers.reduce((s, m) => s + m.additionalPrice, 0)) *
    item.quantity;

  return (
    <div className="space-y-1">
      <div className="flex items-start gap-3">
        {/* Thumbnail */}
        {item.imageUrl && (
          <img
            src={item.imageUrl}
            alt={item.menuItemName}
            className="h-14 w-14 rounded-md object-cover flex-shrink-0"
          />
        )}

        <div className="flex-1 min-w-0">
          <p className="font-medium text-sm leading-tight truncate">{item.menuItemName}</p>

          {/* Modifier list */}
          {item.selectedModifiers.length > 0 && (
            <p className="text-xs text-muted-foreground mt-0.5">
              {item.selectedModifiers.map((m) => m.name).join(", ")}
            </p>
          )}

          {/* Quantity stepper + price */}
          <div className="flex items-center justify-between mt-2">
            <div className="flex items-center gap-1">
              <Button
                variant="outline"
                size="icon"
                className="h-6 w-6"
                onClick={() => updateQuantity(item.key, item.quantity - 1)}
                aria-label="Decrease quantity"
              >
                <Minus className="h-3 w-3" />
              </Button>
              <span className="w-6 text-center text-sm font-medium">{item.quantity}</span>
              <Button
                variant="outline"
                size="icon"
                className="h-6 w-6"
                onClick={() => updateQuantity(item.key, item.quantity + 1)}
                aria-label="Increase quantity"
              >
                <Plus className="h-3 w-3" />
              </Button>
            </div>

            <div className="flex items-center gap-2">
              <span className="text-sm font-semibold">{formatCurrency(lineTotal)}</span>
              <Button
                variant="ghost"
                size="icon"
                className="h-6 w-6 text-destructive hover:text-destructive"
                onClick={() => removeItem(item.key)}
                aria-label="Remove item"
              >
                <Trash2 className="h-3 w-3" />
              </Button>
            </div>
          </div>
        </div>
      </div>
      <Separator />
    </div>
  );
}
