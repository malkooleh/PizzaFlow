import { cn } from "@/lib/utils";
import { OrderStatus, OrderType } from "@/types/enums";
import {
  Clock,
  CheckCircle2,
  ChefHat,
  Package,
  Truck,
  PackageCheck,
  Home,
  XCircle,
  PartyPopper,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";

interface TimelineStep {
  status: OrderStatus;
  label: string;
  icon: LucideIcon;
}

const DELIVERY_STEPS: TimelineStep[] = [
  { status: OrderStatus.PENDING, label: "Order Placed", icon: Clock },
  { status: OrderStatus.CONFIRMED, label: "Confirmed", icon: CheckCircle2 },
  { status: OrderStatus.PREPARING, label: "Preparing", icon: ChefHat },
  { status: OrderStatus.READY, label: "Ready", icon: Package },
  { status: OrderStatus.OUT_FOR_DELIVERY, label: "Out for Delivery", icon: Truck },
  { status: OrderStatus.DELIVERED, label: "Delivered", icon: Home },
  { status: OrderStatus.COMPLETED, label: "Completed", icon: PartyPopper },
];

const PICKUP_STEPS: TimelineStep[] = [
  { status: OrderStatus.PENDING, label: "Order Placed", icon: Clock },
  { status: OrderStatus.CONFIRMED, label: "Confirmed", icon: CheckCircle2 },
  { status: OrderStatus.PREPARING, label: "Preparing", icon: ChefHat },
  { status: OrderStatus.READY, label: "Ready for Pickup", icon: Package },
  { status: OrderStatus.PICKED_UP, label: "Picked Up", icon: PackageCheck },
  { status: OrderStatus.COMPLETED, label: "Completed", icon: PartyPopper },
];

interface OrderTimelineProps {
  status: OrderStatus;
  orderType: OrderType;
  className?: string;
}

export function OrderTimeline({ status, orderType, className }: OrderTimelineProps) {
  if (status === OrderStatus.CANCELLED) {
    return (
      <div className={cn("flex items-center gap-3 py-4", className)}>
        <XCircle className="h-6 w-6 text-destructive shrink-0" />
        <span className="font-medium text-destructive">Order Cancelled</span>
      </div>
    );
  }

  const steps =
    orderType === OrderType.DELIVERY ? DELIVERY_STEPS : PICKUP_STEPS;

  const currentIndex = steps.findIndex((s) => s.status === status);
  // If the current status isn't in our steps (shouldn't happen), treat all as upcoming
  const activeIndex = currentIndex === -1 ? -1 : currentIndex;

  return (
    <ol className={cn("flex flex-col gap-0", className)}>
      {steps.map((step, index) => {
        const isCompleted = index < activeIndex;
        const isActive = index === activeIndex;
        const isLast = index === steps.length - 1;
        const Icon = step.icon;

        return (
          <li key={step.status} className="flex gap-4">
            {/* Left column: icon + connector line */}
            <div className="flex flex-col items-center">
              <div
                className={cn(
                  "flex h-8 w-8 shrink-0 items-center justify-center rounded-full border-2 transition-colors",
                  isCompleted &&
                    "border-primary bg-primary text-primary-foreground",
                  isActive &&
                    "border-primary bg-background text-primary ring-4 ring-primary/20",
                  !isCompleted && !isActive &&
                    "border-muted bg-muted/40 text-muted-foreground",
                )}
              >
                <Icon className="h-4 w-4" />
              </div>
              {!isLast && (
                <div
                  className={cn(
                    "w-0.5 flex-1 my-1 min-h-[1.25rem]",
                    isCompleted ? "bg-primary" : "bg-muted",
                  )}
                />
              )}
            </div>

            {/* Right column: label */}
            <div className={cn("pb-4 pt-0.5", isLast && "pb-0")}>
              <p
                className={cn(
                  "text-sm font-medium leading-8",
                  isActive && "text-foreground",
                  isCompleted && "text-muted-foreground",
                  !isCompleted && !isActive && "text-muted-foreground/60",
                )}
              >
                {step.label}
              </p>
            </div>
          </li>
        );
      })}
    </ol>
  );
}
