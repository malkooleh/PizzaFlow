import { Separator } from "@/components/ui/separator";
import { formatCurrency } from "@/lib/format";
import { useCartStore } from "@/stores/cart.store";
import type { OrderType, PaymentMethodType } from "@/types/enums";

const ORDER_TYPE_LABELS: Record<string, string> = {
  DELIVERY: "Delivery",
  PICKUP: "Pickup",
  DINE_IN: "Dine-In",
  SCHEDULED: "Scheduled",
};

const PAYMENT_LABELS: Record<string, string> = {
  CREDIT_CARD: "Credit Card",
  DEBIT_CARD: "Debit Card",
  PAYPAL: "PayPal",
  APPLE_PAY: "Apple Pay",
  GOOGLE_PAY: "Google Pay",
  CASH_ON_DELIVERY: "Cash on Delivery",
};

interface OrderReviewProps {
  orderType: OrderType;
  paymentMethod: PaymentMethodType;
  deliveryAddress?: string;
  tableNumber?: string;
  scheduledTime?: string;
}

export function OrderReview({
  orderType,
  paymentMethod,
  deliveryAddress,
  tableNumber,
  scheduledTime,
}: OrderReviewProps) {
  const { items, subtotal, tax, deliveryFee, total } = useCartStore();

  return (
    <div className="space-y-4">
      {/* Items */}
      <div className="space-y-2">
        <h3 className="font-medium text-sm uppercase tracking-wide text-muted-foreground">
          Items
        </h3>
        {items.map((item) => {
          const mods = item.selectedModifiers.reduce((s, m) => s + m.additionalPrice, 0);
          const lineTotal = (item.basePrice + mods) * item.quantity;
          return (
            <div key={item.key} className="flex justify-between text-sm">
              <span>
                {item.quantity}× {item.menuItemName}
                {item.selectedModifiers.length > 0 && (
                  <span className="text-muted-foreground">
                    {" "}+{item.selectedModifiers.map((m) => m.name).join(", ")}
                  </span>
                )}
              </span>
              <span className="font-medium flex-shrink-0 ml-4">{formatCurrency(lineTotal)}</span>
            </div>
          );
        })}
      </div>

      <Separator />

      {/* Totals */}
      <div className="space-y-1.5 text-sm">
        <div className="flex justify-between">
          <span className="text-muted-foreground">Subtotal</span>
          <span>{formatCurrency(subtotal())}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">Tax (10%)</span>
          <span>{formatCurrency(tax())}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">Delivery fee</span>
          <span>{deliveryFee() > 0 ? formatCurrency(deliveryFee()) : "–"}</span>
        </div>
        <div className="flex justify-between font-semibold text-base pt-1">
          <span>Total</span>
          <span>{formatCurrency(total())}</span>
        </div>
      </div>

      <Separator />

      {/* Delivery details */}
      <div className="space-y-1.5 text-sm">
        <h3 className="font-medium text-sm uppercase tracking-wide text-muted-foreground">
          Delivery details
        </h3>
        <div className="flex justify-between">
          <span className="text-muted-foreground">Order type</span>
          <span>{ORDER_TYPE_LABELS[orderType] ?? orderType}</span>
        </div>
        {deliveryAddress && (
          <div className="flex justify-between">
            <span className="text-muted-foreground">Address</span>
            <span className="text-right max-w-[60%]">{deliveryAddress}</span>
          </div>
        )}
        {tableNumber && (
          <div className="flex justify-between">
            <span className="text-muted-foreground">Table</span>
            <span>{tableNumber}</span>
          </div>
        )}
        {scheduledTime && (
          <div className="flex justify-between">
            <span className="text-muted-foreground">Scheduled for</span>
            <span>{new Date(scheduledTime).toLocaleString()}</span>
          </div>
        )}
        <div className="flex justify-between">
          <span className="text-muted-foreground">Payment</span>
          <span>{PAYMENT_LABELS[paymentMethod] ?? paymentMethod}</span>
        </div>
      </div>
    </div>
  );
}
