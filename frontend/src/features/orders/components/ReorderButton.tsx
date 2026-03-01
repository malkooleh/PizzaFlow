import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { useCartStore } from "@/stores/cart.store";
import type { OrderResponse } from "@/types/models";
import { RotateCcw } from "lucide-react";
import { useNavigate } from "@tanstack/react-router";
import { toast } from "sonner";

interface ReorderButtonProps {
  order: OrderResponse;
  variant?: "default" | "outline" | "ghost";
  size?: "default" | "sm" | "lg";
}

export function ReorderButton({
  order,
  variant = "outline",
  size = "sm",
}: ReorderButtonProps) {
  const { addItem, wouldClearCart } = useCartStore();
  const navigate = useNavigate();
  const [confirmOpen, setConfirmOpen] = useState(false);

  function doReorder() {
    const restaurantId = String(order.restaurantId);
    for (const item of order.items) {
      addItem(restaurantId, {
        menuItemId: item.menuItemId,
        menuItemName: item.menuItemName,
        basePrice: item.unitPrice,
        quantity: item.quantity,
        selectedModifiers: [],
        specialInstructions: item.specialInstructions ?? undefined,
      });
    }
    toast.success("Items added to your cart!");
    void navigate({ to: "/checkout" });
  }

  function handleClick() {
    if (wouldClearCart(String(order.restaurantId))) {
      setConfirmOpen(true);
    } else {
      doReorder();
    }
  }

  return (
    <>
      <Button variant={variant} size={size} onClick={handleClick}>
        <RotateCcw className="mr-2 h-4 w-4" />
        Reorder
      </Button>

      <Dialog open={confirmOpen} onOpenChange={setConfirmOpen}>
        <DialogContent className="sm:max-w-sm">
          <DialogHeader>
            <DialogTitle>Clear current cart?</DialogTitle>
            <DialogDescription>
              Your cart contains items from a different restaurant. Reordering will
              replace them with items from this order.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setConfirmOpen(false)}>
              Keep Cart
            </Button>
            <Button
              onClick={() => {
                setConfirmOpen(false);
                doReorder();
              }}
            >
              Yes, Reorder
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
