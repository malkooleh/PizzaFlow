import { cn } from "@/lib/utils";
import { PaymentMethodType } from "@/types/enums";

interface PaymentMethodSelectorProps {
  value: PaymentMethodType;
  onChange: (method: PaymentMethodType) => void;
}

const METHODS: { type: PaymentMethodType; label: string; icon: string }[] = [
  { type: PaymentMethodType.CREDIT_CARD, label: "Credit card", icon: "💳" },
  { type: PaymentMethodType.DEBIT_CARD, label: "Debit card", icon: "💳" },
  { type: PaymentMethodType.PAYPAL, label: "PayPal", icon: "🅿️" },
  { type: PaymentMethodType.APPLE_PAY, label: "Apple Pay", icon: "🍎" },
  { type: PaymentMethodType.GOOGLE_PAY, label: "Google Pay", icon: "🔵" },
  { type: PaymentMethodType.CASH_ON_DELIVERY, label: "Cash on delivery", icon: "💵" },
];

export function PaymentMethodSelector({ value, onChange }: PaymentMethodSelectorProps) {
  return (
    <div className="grid grid-cols-2 gap-2">
      {METHODS.map((m) => (
        <button
          key={m.type}
          type="button"
          onClick={() => onChange(m.type)}
          className={cn(
            "flex items-center gap-2 rounded-lg border px-3 py-2.5 text-left text-sm transition-colors",
            value === m.type
              ? "border-primary bg-primary/5 font-medium text-primary"
              : "border-border hover:border-primary/50 hover:bg-muted/50",
          )}
        >
          <span className="text-lg leading-none">{m.icon}</span>
          <span>{m.label}</span>
        </button>
      ))}
    </div>
  );
}
