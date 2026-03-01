import { Separator } from "@/components/ui/separator";
import { formatCurrency } from "@/lib/format";
import { useCartStore } from "@/stores/cart.store";

export function CartSummary() {
  const { subtotal, tax, deliveryFee, total } = useCartStore();

  const sub = subtotal();
  const t = tax();
  const fee = deliveryFee();
  const tot = total();

  return (
    <div className="space-y-2 text-sm">
      <div className="flex justify-between">
        <span className="text-muted-foreground">Subtotal</span>
        <span>{formatCurrency(sub)}</span>
      </div>
      <div className="flex justify-between">
        <span className="text-muted-foreground">Tax (10%)</span>
        <span>{formatCurrency(t)}</span>
      </div>
      <div className="flex justify-between">
        <span className="text-muted-foreground">Delivery fee</span>
        <span>{fee > 0 ? formatCurrency(fee) : "–"}</span>
      </div>
      <Separator />
      <div className="flex justify-between font-semibold text-base">
        <span>Total</span>
        <span>{formatCurrency(tot)}</span>
      </div>
    </div>
  );
}
