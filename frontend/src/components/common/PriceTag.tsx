import { cn } from "@/lib/utils";
import { formatCurrency } from "@/lib/format";

interface PriceTagProps {
  amount: number;
  /** Optional additional price displayed as a delta (e.g. modifier: +$1.50) */
  delta?: number;
  className?: string;
  size?: "sm" | "md" | "lg";
}

const sizeClasses = {
  sm: "text-sm",
  md: "text-base",
  lg: "text-lg font-semibold",
};

export function PriceTag({ amount, delta, className, size = "md" }: PriceTagProps) {
  return (
    <span className={cn("font-medium tabular-nums", sizeClasses[size], className)}>
      {formatCurrency(amount)}
      {delta !== undefined && delta !== 0 && (
        <span className="ml-1 text-muted-foreground text-sm">
          {delta > 0 ? "+" : ""}
          {formatCurrency(delta)}
        </span>
      )}
    </span>
  );
}
