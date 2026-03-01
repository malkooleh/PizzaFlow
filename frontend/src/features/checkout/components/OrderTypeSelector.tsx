import { cn } from "@/lib/utils";
import { OrderType } from "@/types/enums";

interface OrderTypeSelectorProps {
  value: OrderType;
  onChange: (type: OrderType) => void;
}

const OPTIONS: { type: OrderType; label: string; description: string; icon: string }[] = [
  { type: OrderType.DELIVERY, label: "Delivery", description: "To your door", icon: "🛵" },
  { type: OrderType.PICKUP, label: "Pickup", description: "Ready in-store", icon: "🏠" },
  { type: OrderType.DINE_IN, label: "Dine-In", description: "Eat at the restaurant", icon: "🍽️" },
  { type: OrderType.SCHEDULED, label: "Scheduled", description: "Order ahead", icon: "🕐" },
];

export function OrderTypeSelector({ value, onChange }: OrderTypeSelectorProps) {
  return (
    <div className="grid grid-cols-2 gap-3">
      {OPTIONS.map((opt) => (
        <button
          key={opt.type}
          type="button"
          onClick={() => onChange(opt.type)}
          className={cn(
            "flex items-center gap-3 rounded-lg border p-3 text-left transition-colors",
            value === opt.type
              ? "border-primary bg-primary/5 text-primary"
              : "border-border hover:border-primary/50 hover:bg-muted/50",
          )}
        >
          <span className="text-2xl leading-none">{opt.icon}</span>
          <div>
            <p className="font-medium text-sm">{opt.label}</p>
            <p className="text-xs text-muted-foreground">{opt.description}</p>
          </div>
        </button>
      ))}
    </div>
  );
}
