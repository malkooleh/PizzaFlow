import { useState } from "react";
import { useNavigate } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { useCartStore } from "@/stores/cart.store";
import { useCreateOrder } from "@/hooks/use-orders";
import { useProcessPayment } from "@/hooks/use-payments";
import { getCustomerDbId } from "@/lib/auth";
import { OrderType, PaymentMethodType } from "@/types/enums";
import type { OrderResponse } from "@/types/models";
import { OrderTypeSelector } from "./OrderTypeSelector";
import { DeliveryAddressForm, type DeliveryAddressValues } from "./DeliveryAddressForm";
import { TableNumberInput } from "./TableNumberInput";
import { ScheduledTimeSelector } from "./ScheduledTimeSelector";
import { PaymentMethodSelector } from "./PaymentMethodSelector";
import { OrderReview } from "./OrderReview";
import { OrderConfirmation } from "./OrderConfirmation";

type Step = "details" | "review" | "confirmed";

export function CheckoutPage() {
  const navigate = useNavigate();
  const { items, restaurantId, clear, total } = useCartStore();
  const auth = useAuth();
  const createOrder = useCreateOrder();
  const processPayment = useProcessPayment();

  // Form state
  const [step, setStep] = useState<Step>("details");
  const [orderType, setOrderType] = useState<OrderType>(OrderType.DELIVERY);
  const [address, setAddress] = useState<DeliveryAddressValues | null>(null);
  const [tableNumber, setTableNumber] = useState("");
  const [scheduledTime, setScheduledTime] = useState("");
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethodType>(
    PaymentMethodType.CREDIT_CARD,
  );
  const [placedOrder, setPlacedOrder] = useState<OrderResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Redirect if cart is empty (and not yet confirmed)
  if (items.length === 0 && step !== "confirmed") {
    void navigate({ to: "/menu" });
    return null;
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  function buildDeliveryAddress(): string | undefined {
    if (orderType !== OrderType.DELIVERY || !address) return undefined;
    const parts = [address.street, address.district, address.city, address.zipCode].filter(Boolean);
    return parts.join(", ");
  }

  function canAdvanceFromDetails(): boolean {
    if (orderType === OrderType.DELIVERY) return address !== null;
    if (orderType === OrderType.DINE_IN) return tableNumber.trim().length > 0;
    if (orderType === OrderType.SCHEDULED) return scheduledTime.length > 0;
    return true; // PICKUP
  }

  async function handlePlaceOrder() {
    if (!restaurantId) return;
    setError(null);
    try {
      const customerId = getCustomerDbId(auth.user);
      const order = await createOrder.mutateAsync({
        customerId,
        restaurantId,
        orderType,
        items: items.map((item) => ({
          menuItemId: item.menuItemId,
          menuItemName: item.menuItemName,
          quantity: item.quantity,
          unitPrice:
            item.basePrice +
            item.selectedModifiers.reduce((sum, m) => sum + m.additionalPrice, 0),
          specialInstructions: item.specialInstructions,
        })),
        deliveryAddress: buildDeliveryAddress(),
        tableNumber: orderType === OrderType.DINE_IN ? tableNumber : undefined,
        scheduledTime: orderType === OrderType.SCHEDULED ? scheduledTime : undefined,
      });
      await processPayment.mutateAsync({
        orderId: order.id,
        customerId,
        amount: total(),   // subtotal + 10% tax + delivery fee from cart store
        paymentMethodType: paymentMethod,
      });
      setPlacedOrder(order);
      clear();
      setStep("confirmed");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to place order. Please try again.");
    }
  }

  // ── Render ─────────────────────────────────────────────────────────────────

  if (step === "confirmed" && placedOrder) {
    return (
      <div className="container max-w-2xl py-8">
        <OrderConfirmation order={placedOrder} />
      </div>
    );
  }

  return (
    <div className="container max-w-2xl py-8 space-y-8">
      <h1 className="text-2xl font-bold">Checkout</h1>

      {/* ── Step 1: Order details ── */}
      <section className="space-y-4">
        <h2 className="font-semibold text-lg">1. Order type</h2>
        <OrderTypeSelector value={orderType} onChange={setOrderType} />

        {orderType === OrderType.DELIVERY && (
          <>
            <Separator />
            <h2 className="font-semibold">Delivery address</h2>
            <DeliveryAddressForm
              defaultValues={address ?? undefined}
              onSubmit={(values) => {
                setAddress(values);
              }}
              submitLabel="Save address"
            />
          </>
        )}

        {orderType === OrderType.DINE_IN && (
          <>
            <Separator />
            <TableNumberInput value={tableNumber} onChange={setTableNumber} />
          </>
        )}

        {orderType === OrderType.SCHEDULED && (
          <>
            <Separator />
            <ScheduledTimeSelector value={scheduledTime} onChange={setScheduledTime} />
          </>
        )}
      </section>

      {/* ── Step 1 → 2 button ── */}
      {step === "details" && (
        <Button
          className="w-full"
          disabled={!canAdvanceFromDetails()}
          onClick={() => setStep("review")}
        >
          Review order
        </Button>
      )}

      {/* ── Step 2: Review + payment ── */}
      {step === "review" && (
        <>
          <Separator />
          <section className="space-y-4">
            <h2 className="font-semibold text-lg">2. Review your order</h2>
            <OrderReview
              orderType={orderType}
              paymentMethod={paymentMethod}
              deliveryAddress={buildDeliveryAddress()}
              tableNumber={orderType === OrderType.DINE_IN ? tableNumber : undefined}
              scheduledTime={orderType === OrderType.SCHEDULED ? scheduledTime : undefined}
            />
          </section>

          <Separator />

          <section className="space-y-4">
            <h2 className="font-semibold text-lg">3. Payment method</h2>
            <PaymentMethodSelector value={paymentMethod} onChange={setPaymentMethod} />
          </section>

          {error && (
            <p className="text-sm text-destructive rounded-md border border-destructive/30 bg-destructive/10 p-3">
              {error}
            </p>
          )}

          <div className="flex gap-3">
            <Button variant="outline" onClick={() => setStep("details")}>
              Back
            </Button>
            <Button
              className="flex-1"
              disabled={createOrder.isPending || processPayment.isPending}
              onClick={() => void handlePlaceOrder()}
            >
              {createOrder.isPending
                ? "Placing order…"
                : processPayment.isPending
                  ? "Processing payment…"
                  : "Place order"}
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
