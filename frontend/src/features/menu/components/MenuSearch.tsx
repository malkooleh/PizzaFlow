import { useRef, useEffect, useState } from "react";
import { Search, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface MenuSearchProps {
  value: string;
  onChange: (value: string) => void;
  /** Debounce delay in ms (default: 350) */
  debounceMs?: number;
}

/**
 * Controlled search input that debounces the onChange call.
 * The parent manages the canonical (debounced) query value in URL state.
 */
export function MenuSearch({ value, onChange, debounceMs = 350 }: MenuSearchProps) {
  // Local state tracks immediate keystrokes; parent state is debounced.
  const [local, setLocal] = useState(value);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Sync if parent value changes (e.g., URL navigation).
  useEffect(() => {
    setLocal(value);
  }, [value]);

  const handleChange = (next: string) => {
    setLocal(next);
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => onChange(next), debounceMs);
  };

  const clear = () => {
    handleChange("");
  };

  return (
    <div className="relative flex items-center">
      <Search
        className="absolute left-3 h-4 w-4 text-muted-foreground pointer-events-none"
        aria-hidden="true"
      />
      <Input
        type="search"
        placeholder="Search menu…"
        value={local}
        onChange={(e) => handleChange(e.target.value)}
        className="pl-9 pr-9"
        aria-label="Search menu items"
      />
      {local && (
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="absolute right-1 h-7 w-7"
          onClick={clear}
          aria-label="Clear search"
        >
          <X className="h-3.5 w-3.5" />
        </Button>
      )}
    </div>
  );
}
