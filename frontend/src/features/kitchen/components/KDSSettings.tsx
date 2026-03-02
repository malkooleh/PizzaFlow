import { Settings } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { useKdsStore, type KdsLayout } from "@/stores/kds.store";

// ── Subcomponents ─────────────────────────────────────────────────────────────

interface ToggleRowProps {
  id: string;
  label: string;
  description: string;
  checked: boolean;
  onChange: () => void;
}

function ToggleRow({ id, label, description, checked, onChange }: ToggleRowProps) {
  return (
    <div className="flex items-center justify-between gap-4">
      <div className="space-y-0.5 min-w-0">
        <Label htmlFor={id} className="text-sm font-medium cursor-pointer">
          {label}
        </Label>
        <p className="text-xs text-muted-foreground">{description}</p>
      </div>
      <button
        id={id}
        role="switch"
        aria-checked={checked}
        type="button"
        onClick={onChange}
        className={`relative inline-flex h-5 w-9 shrink-0 cursor-pointer items-center rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 ${
          checked ? "bg-primary" : "bg-input"
        }`}
      >
        <span
          className={`pointer-events-none block h-4 w-4 rounded-full bg-background shadow-lg ring-0 transition-transform ${
            checked ? "translate-x-4" : "translate-x-0"
          }`}
        />
      </button>
    </div>
  );
}

// ── Layout option ─────────────────────────────────────────────────────────────

const LAYOUT_OPTIONS: { value: KdsLayout; label: string }[] = [
  { value: "kanban", label: "Kanban" },
  { value: "list", label: "List" },
  { value: "grid", label: "Grid" },
];

// ── KDSSettings ───────────────────────────────────────────────────────────────

/**
 * Popover panel for KDS display preferences.
 *
 * Persisted to localStorage via the kds.store Zustand store.
 * Preferences: column layout (kanban/list/grid), audio alert sounds, auto-scroll.
 */
export function KDSSettings() {
  const { layout, audioAlertsEnabled, autoScroll, setLayout, toggleAudioAlerts, toggleAutoScroll } =
    useKdsStore();

  return (
    <Sheet>
      <SheetTrigger asChild>
        <Button
          variant="outline"
          size="sm"
          aria-label="KDS display settings"
          className="gap-1.5"
        >
          <Settings className="h-3.5 w-3.5" aria-hidden="true" />
          <span className="hidden sm:inline text-xs">Settings</span>
        </Button>
      </SheetTrigger>
      <SheetContent side="right" className="w-72 space-y-4">
        <SheetHeader>
          <SheetTitle>Display Settings</SheetTitle>
          <p className="text-xs text-muted-foreground">
            Preferences are saved automatically.
          </p>
        </SheetHeader>

        <Separator />

        {/* Layout selector */}
        <div className="space-y-2">
          <Label className="text-xs uppercase tracking-wide text-muted-foreground">
            Board layout
          </Label>
          <div className="flex gap-1">
            {LAYOUT_OPTIONS.map(({ value, label }) => (
              <Button
                key={value}
                variant={layout === value ? "default" : "outline"}
                size="sm"
                className="flex-1 text-xs h-8"
                onClick={() => setLayout(value)}
                aria-pressed={layout === value}
              >
                {label}
              </Button>
            ))}
          </div>
        </div>

        <Separator />

        {/* Toggle rows */}
        <div className="space-y-3">
          <ToggleRow
            id="kds-audio-toggle"
            label="Audio alerts"
            description="Play a sound when a new order arrives"
            checked={audioAlertsEnabled}
            onChange={toggleAudioAlerts}
          />
          <ToggleRow
            id="kds-autoscroll-toggle"
            label="Auto-scroll"
            description="Scroll the board when new orders arrive"
            checked={autoScroll}
            onChange={toggleAutoScroll}
          />
        </div>
      </SheetContent>
    </Sheet>
  );
}
