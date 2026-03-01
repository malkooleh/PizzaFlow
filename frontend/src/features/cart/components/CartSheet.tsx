import { ShoppingCart } from "lucide-react";
import { useNavigate } from "@tanstack/react-router";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useCartStore } from "@/stores/cart.store";
import { CartItemRow } from "./CartItemRow";
import { CartSummary } from "./CartSummary";

interface CartSheetProps {
  /** Optional trigger element — defaults to a cart icon button */
  readonly children?: React.ReactNode;
}

export function CartSheet({ children }: CartSheetProps) {
  const navigate = useNavigate();
  const { items, totalItems, clear } = useCartStore();
  const count = totalItems();

  const defaultTrigger = (
    <Button variant="outline" size="icon" className="relative">
      <ShoppingCart className="h-5 w-5" />
      {count > 0 && (
        <Badge className="absolute -top-2 -right-2 h-5 w-5 rounded-full p-0 flex items-center justify-center text-xs">
          {count > 99 ? "99+" : count}
        </Badge>
      )}
      <span className="sr-only">Open cart ({count} items)</span>
    </Button>
  );

  return (
    <Sheet>
      <SheetTrigger asChild>{children ?? defaultTrigger}</SheetTrigger>

      <SheetContent side="right" className="flex flex-col w-full sm:max-w-md p-0">
        <SheetHeader className="px-6 pt-6 pb-2">
          <SheetTitle className="flex items-center gap-2">
            <ShoppingCart className="h-5 w-5" />
            Your Cart
            {count > 0 && (
              <Badge variant="secondary" className="ml-auto">
                {count} item{count === 1 ? "" : "s"}
              </Badge>
            )}
          </SheetTitle>
        </SheetHeader>

        {/* Items list */}
        <div className="flex-1 overflow-y-auto px-6 py-2 space-y-4">
          {items.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full gap-3 py-12 text-center">
              <ShoppingCart className="h-12 w-12 text-muted-foreground/40" />
              <p className="text-muted-foreground">Your cart is empty</p>
              <p className="text-sm text-muted-foreground">
                Browse the menu and add some items!
              </p>
            </div>
          ) : (
            items.map((item) => <CartItemRow key={item.key} item={item} />)
          )}
        </div>

        {/* Footer */}
        {items.length > 0 && (
          <div className="border-t px-6 py-4 space-y-4">
            <CartSummary />

            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                className="flex-1"
                onClick={() => clear()}
              >
                Clear cart
              </Button>
              <Button
                className="flex-2 flex-grow"
                onClick={() => {
                  navigate({ to: "/checkout" });
                }}
              >
                Checkout
              </Button>
            </div>
          </div>
        )}
      </SheetContent>
    </Sheet>
  );
}
