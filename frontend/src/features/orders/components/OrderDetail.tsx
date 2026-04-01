import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/common/StatusBadge";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { OrderStatus, OrderType, PaymentMethodType } from "@/types/enums";
import { useActiveOrderTracking } from "@/hooks/use-orders";
import { usePaymentByOrder } from "@/hooks/use-payments";
import { OrderTimeline } from "./OrderTimeline";
import { CancelOrderDialog } from "./CancelOrderDialog";
import { ReorderButton } from "./ReorderButton";
import {
  MapPin,
  UtensilsCrossed,
  Hash,
  CreditCard,
  Clock,
  RefreshCw,
} from "lucide-react";

const CANCELLABLE_STATUSES = new Set([OrderStatus.PENDING, OrderStatus.CONFIRMED]);

const REORDERABLE_STATUSES = new Set([
  OrderStatus.DELIVERED,
  OrderStatus.COMPLETED,
  OrderStatus.CANCELLED,
]);

const PAYMENT_METHOD_LABELS: Record<PaymentMethodType, string> = {
  [PaymentMethodType.CREDIT_CARD]: "Credit Card",
  [PaymentMethodType.DEBIT_CARD]: "Debit Card",
  [PaymentMethodType.PAYPAL]: "PayPal",
  [PaymentMethodType.APPLE_PAY]: "Apple Pay",
  [PaymentMethodType.GOOGLE_PAY]: "Google Pay",
  [PaymentMethodType.CASH_ON_DELIVERY]: "Cash on Delivery",
};

interface OrderDetailProps {
  orderId: number;
}

export function OrderDetail({ orderId }: OrderDetailProps) {
  const { data: order, isLoading, error, isFetching } = useActiveOrderTracking(orderId);
  const { data: payment } = usePaymentByOrder(orderId);

  if (isLoading) {
    return <OrderDetailSkeleton />;
  }

  if (error || !order) {
    return (
      <div className="rounded-lg border border-destructive/30 bg-destructive/5 p-6 text-center">
        <p className="text-destructive font-medium">Failed to load order details.</p>
        <p className="text-sm text-muted-foreground mt-1">Please refresh the page to try again.</p>
      </div>
    );
  }

  const isCancellable = CANCELLABLE_STATUSES.has(order.status);
  const isReorderable = REORDERABLE_STATUSES.has(order.status);

  return (
    <div className="space-y-6">
      {/* ── Header ── */}
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div className="space-y-1">
          <div className="flex items-center gap-2 flex-wrap">
            <h1 className="text-xl font-bold">{order.orderNumber}</h1>
            <StatusBadge status={order.status} />
            {isFetching && (
              <span className="text-xs text-muted-foreground flex items-center gap-1">
                <RefreshCw className="h-3 w-3 animate-spin" />
                Updating
              </span>
            )}
          </div>
          <p className="text-sm text-muted-foreground flex items-center gap-1">
            <Clock className="h-3.5 w-3.5" />
            Placed {formatDateTime(order.createdAt)}
          </p>
        </div>

        <div className="flex gap-2 flex-wrap">
          {isCancellable && (
            <CancelOrderDialog
              orderId={order.id}
              orderNumber={order.orderNumber}
            />
          )}
          {isReorderable && <ReorderButton order={order} />}
        </div>
      </div>

      <Separator />

      <div className="grid gap-6 md:grid-cols-[1fr_280px]">
        {/* ── Left column ── */}
        <div className="space-y-6">
          {/* Items */}
          <section>
            <h2 className="font-semibold mb-3">Items</h2>
            <div className="divide-y rounded-lg border overflow-hidden">
              {order.items.map((item) => (
                <div key={item.id} className="flex items-center justify-between px-4 py-3">
                  <div className="flex items-center gap-3">
                    <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-muted text-xs font-semibold">
                      {item.quantity}
                    </span>
                    <div>
                      <p className="text-sm font-medium">{item.menuItemName}</p>
                      {item.specialInstructions && (
                        <p className="text-xs text-muted-foreground">{item.specialInstructions}</p>
                      )}
                    </div>
                  </div>
                  <span className="text-sm font-medium">
                    {formatCurrency(item.unitPrice * item.quantity)}
                  </span>
                </div>
              ))}
            </div>
          </section>

          {/* Pricing */}
          <section>
            <h2 className="font-semibold mb-3">Summary</h2>
            <div className="rounded-lg border p-4 space-y-2 text-sm">
              <div className="flex justify-between text-muted-foreground">
                <span>Subtotal</span>
                <span>{formatCurrency(order.subtotal)}</span>
              </div>
              <div className="flex justify-between text-muted-foreground">
                <span>Tax</span>
                <span>{formatCurrency(order.tax)}</span>
              </div>
              {order.orderType === OrderType.DELIVERY && (
                <div className="flex justify-between text-muted-foreground">
                  <span>Delivery fee</span>
                  <span>{formatCurrency(order.deliveryFee)}</span>
                </div>
              )}
              <Separator />
              <div className="flex justify-between font-semibold">
                <span>Total</span>
                <span>{formatCurrency(order.totalAmount)}</span>
              </div>
            </div>
          </section>

          {/* Delivery / Dine-in info */}
          {order.deliveryAddress && (
            <section>
              <h2 className="font-semibold mb-3">Delivery Address</h2>
              <p className="text-sm text-muted-foreground flex items-start gap-2">
                <MapPin className="h-4 w-4 mt-0.5 shrink-0 text-primary" />
                {order.deliveryAddress}
              </p>
            </section>
          )}

          {order.tableNumber && (
            <section>
              <h2 className="font-semibold mb-3">Table</h2>
              <p className="text-sm text-muted-foreground flex items-center gap-2">
                <UtensilsCrossed className="h-4 w-4 text-primary" />
                Table {order.tableNumber}
              </p>
            </section>
          )}

          {/* Payment */}
          {payment && (
            <section>
              <h2 className="font-semibold mb-3">Payment</h2>
              <div className="rounded-lg border p-4 space-y-2 text-sm">
                <div className="flex items-center gap-2 text-muted-foreground">
                  <CreditCard className="h-4 w-4" />
                  <span>
                    {PAYMENT_METHOD_LABELS[payment.paymentMethodType] ??
                      payment.paymentMethodType}
                  </span>
                </div>
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Hash className="h-4 w-4" />
                  <span className="font-mono text-xs">{payment.transactionId}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Payment status</span>
                  <StatusBadge status={payment.status} />
                </div>
              </div>
            </section>
          )}
        </div>

        {/* ── Right column: Timeline ── */}
        <aside>
          <h2 className="font-semibold mb-4">Order Status</h2>
          <OrderTimeline status={order.status} orderType={order.orderType} />
        </aside>
      </div>
    </div>
  );
}

function OrderDetailSkeleton() {
  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <Skeleton className="h-7 w-40" />
        <Skeleton className="h-4 w-52" />
      </div>
      <Separator />
      <div className="grid gap-6 md:grid-cols-[1fr_280px]">
        <div className="space-y-4">
          {[1, 2, 3].map((n) => (
            <div key={n} className="flex justify-between">
              <Skeleton className="h-5 w-48" />
              <Skeleton className="h-5 w-16" />
            </div>
          ))}
        </div>
        <div className="space-y-3">
          {[1, 2, 3, 4].map((n) => (
            <Skeleton key={n} className="h-8 w-full" />
          ))}
        </div>
      </div>
    </div>
  );
}
