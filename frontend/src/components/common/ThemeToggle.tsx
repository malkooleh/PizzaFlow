import { Sun, Moon, Monitor } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { useUiStore } from "@/stores/ui.store";

const THEME_CYCLE = ["light", "dark", "system"] as const;
type Theme = (typeof THEME_CYCLE)[number];

const THEME_ICON: Record<Theme, React.ReactNode> = {
  light: <Sun className="h-5 w-5" />,
  dark: <Moon className="h-5 w-5" />,
  system: <Monitor className="h-5 w-5" />,
};

const THEME_LABEL: Record<Theme, string> = {
  light: "Light mode",
  dark: "Dark mode",
  system: "System theme",
};

export function ThemeToggle() {
  const theme = useUiStore((s) => s.theme);
  const setTheme = useUiStore((s) => s.setTheme);

  const nextTheme = (): Theme => {
    const idx = THEME_CYCLE.indexOf(theme);
    return THEME_CYCLE[(idx + 1) % THEME_CYCLE.length];
  };

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          aria-label={THEME_LABEL[theme]}
          onClick={() => setTheme(nextTheme())}
        >
          {THEME_ICON[theme]}
        </Button>
      </TooltipTrigger>
      <TooltipContent>{THEME_LABEL[theme]}</TooltipContent>
    </Tooltip>
  );
}
