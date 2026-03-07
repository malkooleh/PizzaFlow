import { Label } from "@/components/ui/label";
import { TableType } from "@/types/enums";

export const TABLE_TYPE_OPTIONS: { value: TableType | ""; label: string }[] = [
  { value: "", label: "No preference" },
  { value: TableType.INDOOR, label: "Indoor" },
  { value: TableType.OUTDOOR, label: "Outdoor / Patio" },
  { value: TableType.BAR, label: "Bar" },
  { value: TableType.PRIVATE, label: "Private room" },
  { value: TableType.VIP, label: "VIP section" },
];

export interface TableTypePreferenceProps {
  /** Current value. Pass an empty string for "no preference". */
  readonly value: string;
  readonly onChange: (value: string) => void;
  readonly id?: string;
  readonly name?: string;
  readonly disabled?: boolean;
}

/**
 * Optional seating preference selector for table reservations.
 *
 * Renders a labeled `<select>` with INDOOR, OUTDOOR, BAR, PRIVATE, VIP options
 * (plus a "No preference" empty-value option).
 *
 * Integrates with react-hook-form via spread `{...register("preferredTableType")}`,
 * or use `value`/`onChange` for controlled usage.
 */
export function TableTypePreference({
  value,
  onChange,
  id = "preferredTableType",
  name,
  disabled = false,
}: Readonly<TableTypePreferenceProps>) {
  return (
    <div className="space-y-1.5" data-testid="table-type-preference">
      <Label htmlFor={id}>
        Seating preference{" "}
        <span className="text-muted-foreground text-xs">(optional)</span>
      </Label>
      <select
        id={id}
        name={name}
        value={value}
        disabled={disabled}
        onChange={(e) => onChange(e.target.value)}
        className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:opacity-50"
      >
        {TABLE_TYPE_OPTIONS.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}
