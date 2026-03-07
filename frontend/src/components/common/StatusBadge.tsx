import { Badge } from "@/components/ui/badge";
import { OrderStatus, PaymentStatus, BookingStatus, DeliveryStatus } from "@/types/enums";
import { cn } from "@/lib/utils";

type StatusValue = OrderStatus | PaymentStatus | BookingStatus | DeliveryStatus | string;

interface StatusBadgeProps {
  status: StatusValue;
  className?: string;
}

const STATUS_LABELS: Record<string, string> = {
  // Order
  [OrderStatus.PENDING]: "Pending",
  [OrderStatus.CONFIRMED]: "Confirmed",
  [OrderStatus.PREPARING]: "Preparing",
  [OrderStatus.READY]: "Ready",
  [OrderStatus.PICKED_UP]: "Picked Up",
  [OrderStatus.OUT_FOR_DELIVERY]: "Out for Delivery",
  [OrderStatus.DELIVERED]: "Delivered",
  [OrderStatus.COMPLETED]: "Completed",
  [OrderStatus.CANCELLED]: "Cancelled",

  // Payment
  [PaymentStatus.PROCESSING]: "Processing",
  [PaymentStatus.FAILED]: "Payment Failed",
  [PaymentStatus.REFUNDED]: "Refunded",

  // Booking
  [BookingStatus.SEATED]: "Seated",
  [BookingStatus.NO_SHOW]: "No Show",

  // Delivery
  [DeliveryStatus.ASSIGNED]: "Assigned",
  [DeliveryStatus.IN_TRANSIT]: "In Transit",
  [DeliveryStatus.ARRIVED]: "Arrived",
};

const SUCCESS_STATUSES = new Set<string>([
  OrderStatus.COMPLETED, OrderStatus.DELIVERED,
  PaymentStatus.COMPLETED,
  DeliveryStatus.DELIVERED,
  BookingStatus.COMPLETED,
]);
const WARNING_STATUSES = new Set<string>([
  OrderStatus.PENDING, PaymentStatus.PENDING, BookingStatus.PENDING,
  DeliveryStatus.PENDING, DeliveryStatus.ASSIGNED,
]);
const DANGER_STATUSES = new Set<string>([
  OrderStatus.CANCELLED,
  PaymentStatus.FAILED, PaymentStatus.CANCELLED,
  BookingStatus.CANCELLED, BookingStatus.NO_SHOW,
  DeliveryStatus.FAILED, DeliveryStatus.CANCELLED,
]);

function getVariant(status: string): "success" | "warning" | "destructive" | "info" | "secondary" {
  if (SUCCESS_STATUSES.has(status)) return "success";
  if (WARNING_STATUSES.has(status)) return "warning";
  if (DANGER_STATUSES.has(status)) return "destructive";
  return "info";
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
  return (
    <Badge variant={getVariant(status)} className={cn("whitespace-nowrap", className)}>
      {STATUS_LABELS[status] ?? status}
    </Badge>
  );
}
