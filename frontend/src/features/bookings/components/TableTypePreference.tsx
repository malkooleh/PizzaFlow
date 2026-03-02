import { Label } from "@/components/ui/label";
import type { TableType } from "@/types/enums";

// ── Constants ─────────────────────────────────────────────────────────────────

export const TABLE_TYPE_OPTIONS: { value: TableType | ""; label: string }[] = [
  { value: "", label: "No preference" },
  { value: "INDOOR", label: "Indoor" },
  { value: "OUTDOOR", label: "Outdoor / Patio" },
  { value: "BAR", label: "Bar" },
  { value: "PRIVATE", label: "Private room" },
  { value: "VIP", label: "VIP section" },
];

// ── Types ─────────────────────────────────────────────────────────────────────

export interface TableTypePreferenceProps {
  /** Current value. Pass an empty string for "no preference". */
  value: string;
  onChange: (value: string) => void;
  id?: string;
  name?: string;
  disabled?: boolean;
}

// ── TableTypePreference ───────────────────────────────────────────────────────

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
}: TableTypePreferenceProps) {
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
