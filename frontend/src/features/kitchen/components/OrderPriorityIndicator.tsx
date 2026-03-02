import { cn } from "@/lib/utils";
import { OrderPriority } from "@/types/enums";

const PRIORITY_STYLES: Record<OrderPriority, string> = {
  [OrderPriority.LOW]: "bg-slate-100 text-slate-500",
  [OrderPriority.NORMAL]: "bg-blue-50 text-blue-700",
  [OrderPriority.HIGH]: "bg-orange-100 text-orange-700",
  [OrderPriority.URGENT]: "bg-red-100 text-red-700 animate-pulse ring-1 ring-red-300",
};

const PRIORITY_LABELS: Record<OrderPriority, string> = {
  [OrderPriority.LOW]: "Low",
  [OrderPriority.NORMAL]: "Normal",
  [OrderPriority.HIGH]: "High",
  [OrderPriority.URGENT]: "URGENT",
};

interface OrderPriorityIndicatorProps {
  priority: OrderPriority;
  className?: string;
}

/**
 * Compact priority chip — color-coded to reflect order urgency.
 * URGENT priority pulses as a visual alert cue.
 */
export function OrderPriorityIndicator({
  priority,
  className,
}: OrderPriorityIndicatorProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold",
        PRIORITY_STYLES[priority],
        className
      )}
      aria-label={`Priority: ${PRIORITY_LABELS[priority]}`}
    >
      {PRIORITY_LABELS[priority]}
    </span>
  );
}
