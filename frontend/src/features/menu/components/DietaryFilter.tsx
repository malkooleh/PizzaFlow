import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

const DIETARY_FILTERS = [
  { key: "vegetarian", label: "Vegetarian", emoji: "🌿" },
  { key: "vegan", label: "Vegan", emoji: "🌱" },
  { key: "glutenFree", label: "Gluten-Free", emoji: "🌾" },
] as const;

export type DietaryKey = (typeof DIETARY_FILTERS)[number]["key"];

interface DietaryFilterProps {
  active: DietaryKey[];
  onChange: (active: DietaryKey[]) => void;
}

export function DietaryFilter({ active, onChange }: DietaryFilterProps) {
  const toggle = (key: DietaryKey) => {
    if (active.includes(key)) {
      onChange(active.filter((k) => k !== key));
    } else {
      onChange([...active, key]);
    }
  };

  return (
    <div className="flex flex-wrap gap-2" aria-label="Dietary filters">
      {DIETARY_FILTERS.map((f) => {
        const isActive = active.includes(f.key);
        return (
          <Badge
            key={f.key}
            role="checkbox"
            aria-checked={isActive}
            tabIndex={0}
            variant={isActive ? "default" : "outline"}
            className={cn(
              "cursor-pointer select-none gap-1 px-3 py-1 text-xs transition-colors",
              "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            )}
            onClick={() => toggle(f.key)}
            onKeyDown={(e) => e.key === "Enter" && toggle(f.key)}
          >
            <span aria-hidden="true">{f.emoji}</span>
            {f.label}
          </Badge>
        );
      })}
    </div>
  );
}
