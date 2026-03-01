import { CheckCircle2, Package } from "lucide-react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { formatCurrency } from "@/lib/format";
import type { OrderResponse } from "@/types/models";

interface OrderConfirmationProps {
  order: OrderResponse;
}

export function OrderConfirmation({ order }: OrderConfirmationProps) {
  return (
    <div className="flex flex-col items-center text-center gap-6 py-8 max-w-md mx-auto">
      <div className="flex flex-col items-center gap-3">
        <div className="rounded-full bg-green-100 p-4">
          <CheckCircle2 className="h-12 w-12 text-green-600" />
        </div>
        <h1 className="text-2xl font-bold">Order placed!</h1>
        <p className="text-muted-foreground">
          Your order <span className="font-semibold text-foreground">#{order.orderNumber}</span> has
          been received and is being processed.
        </p>
      </div>

      {/* Order summary card */}
      <div className="w-full rounded-xl border p-4 text-left space-y-3">
        <div className="flex items-center gap-2 font-medium">
          <Package className="h-4 w-4" />
          Order summary
        </div>
        <Separator />
        {order.items.map((item) => (
          <div key={item.id} className="flex justify-between text-sm">
            <span>
              {item.quantity}× {item.menuItemName}
            </span>
            <span className="font-medium">{formatCurrency(item.unitPrice * item.quantity)}</span>
          </div>
        ))}
        <Separator />
        <div className="flex justify-between font-semibold">
          <span>Total paid</span>
          <span>{formatCurrency(order.totalAmount)}</span>
        </div>
      </div>

      {/* Estimated time */}
      <p className="text-sm text-muted-foreground">
        Estimated delivery time:{" "}
        <span className="font-medium text-foreground">30–45 minutes</span>
      </p>

      <div className="flex gap-3 w-full">
        <Button variant="outline" className="flex-1" asChild>
          <Link to="/menu">Back to menu</Link>
        </Button>
        <Button className="flex-1" asChild>
          <Link to="/orders/$orderId" params={{ orderId: String(order.id) }}>
            Track order
          </Link>
        </Button>
      </div>
    </div>
  );
}
