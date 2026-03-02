import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";

interface RestaurantFilterProps {
  value: string | undefined;
  onChange: (id: string | undefined) => void;
}

/**
 * Simple restaurant-ID selector for admin-level cross-service dashboards.
 * Accepts a UUID/string restaurant ID manually or clears to show platform-wide data.
 */
export function RestaurantFilter({ value, onChange }: RestaurantFilterProps) {
  return (
    <div className="flex items-end gap-2">
      <div className="space-y-1">
        <Label htmlFor="restaurant-filter" className="text-xs text-muted-foreground">
          Restaurant ID (optional)
        </Label>
        <Input
          id="restaurant-filter"
          placeholder="e.g. abc123 — leave blank for all"
          value={value ?? ""}
          onChange={(e) => onChange(e.target.value || undefined)}
          className="h-8 w-56 text-xs"
        />
      </div>
      {value && (
        <Button
          variant="ghost"
          size="sm"
          className="h-8 text-xs"
          onClick={() => onChange(undefined)}
        >
          Clear
        </Button>
      )}
    </div>
  );
}
