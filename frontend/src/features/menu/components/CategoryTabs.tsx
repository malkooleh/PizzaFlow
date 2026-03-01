import { MenuCategory } from "@/types/enums";
import { cn } from "@/lib/utils";

const CATEGORIES: { value: MenuCategory | "ALL"; label: string; emoji: string }[] = [
  { value: "ALL", label: "All", emoji: "🍽️" },
  { value: MenuCategory.PIZZA, label: "Pizza", emoji: "🍕" },
  { value: MenuCategory.APPETIZER, label: "Starters", emoji: "🥗" },
  { value: MenuCategory.SIDE, label: "Sides", emoji: "🍟" },
  { value: MenuCategory.SALAD, label: "Salads", emoji: "🥙" },
  { value: MenuCategory.DESSERT, label: "Desserts", emoji: "🍰" },
  { value: MenuCategory.DRINK, label: "Drinks", emoji: "🥤" },
];

interface CategoryTabsProps {
  value: MenuCategory | "ALL";
  onChange: (category: MenuCategory | "ALL") => void;
}

export function CategoryTabs({ value, onChange }: CategoryTabsProps) {
  return (
    <nav
      aria-label="Menu categories"
      className="flex gap-2 overflow-x-auto pb-1 scrollbar-none"
    >
      {CATEGORIES.map((cat) => (
        <button
          key={cat.value}
          onClick={() => onChange(cat.value)}
          aria-pressed={value === cat.value}
          className={cn(
            "flex shrink-0 items-center gap-1.5 rounded-full px-4 py-1.5 text-sm font-medium transition-colors",
            "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
            value === cat.value
              ? "bg-primary text-primary-foreground"
              : "bg-muted text-muted-foreground hover:bg-accent hover:text-accent-foreground"
          )}
        >
          <span aria-hidden="true">{cat.emoji}</span>
          {cat.label}
        </button>
      ))}
    </nav>
  );
}

export { CATEGORIES };
