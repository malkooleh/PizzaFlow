import { createFileRoute, Link } from "@tanstack/react-router";
import { ShoppingCart, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { useCartStore } from "@/stores/cart.store";
import { CartItemRow } from "@/features/cart/components/CartItemRow";
import { CartSummary } from "@/features/cart/components/CartSummary";

export const Route = createFileRoute("/cart")({
  component: CartPage,
});

function CartPage() {
  const { items, clear } = useCartStore();

  return (
    <div className="container max-w-xl py-8 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold flex items-center gap-2">
          <ShoppingCart className="h-6 w-6" />
          Your Cart
        </h1>
        {items.length > 0 && (
          <Button variant="ghost" size="sm" className="text-destructive hover:text-destructive" onClick={() => clear()}>
            Clear all
          </Button>
        )}
      </div>

      {items.length === 0 ? (
        <div className="flex flex-col items-center gap-4 py-16 text-center">
          <ShoppingCart className="h-16 w-16 text-muted-foreground/30" />
          <p className="text-lg font-medium">Your cart is empty</p>
          <p className="text-sm text-muted-foreground">Add items from the menu to get started.</p>
          <Button asChild className="mt-2">
            <Link to="/menu">Browse menu</Link>
          </Button>
        </div>
      ) : (
        <>
          <div className="space-y-4">
            {items.map((item) => (
              <CartItemRow key={item.key} item={item} />
            ))}
          </div>

          <Separator />

          <CartSummary />

          <div className="flex flex-col gap-3">
            <Button asChild className="w-full gap-2">
              <Link to="/checkout">
                Proceed to checkout
                <ArrowRight className="h-4 w-4" />
              </Link>
            </Button>
            <Button variant="outline" asChild className="w-full">
              <Link to="/menu">Continue shopping</Link>
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
