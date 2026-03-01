import type { ReactNode } from "react";
import { Button } from "@/components/ui/button";

interface EmptyStateProps {
  /** Main heading text. */
  title: string;
  /** Optional supporting description. */
  description?: string;
  /** Optional action button rendered below the description. */
  action?: {
    label: string;
    onClick: () => void;
  };
  /** Optional custom illustration or icon element. */
  icon?: ReactNode;
  className?: string;
}

export function EmptyState({ title, description, action, icon, className }: EmptyStateProps) {
  return (
    <div
      className={`flex flex-col items-center justify-center gap-4 py-16 text-center ${className ?? ""}`}
    >
      {icon && <div className="text-muted-foreground">{icon}</div>}
      <div className="space-y-1">
        <p className="text-lg font-semibold">{title}</p>
        {description && <p className="text-sm text-muted-foreground max-w-sm">{description}</p>}
      </div>
      {action && (
        <Button onClick={action.onClick} size="sm">
          {action.label}
        </Button>
      )}
    </div>
  );
}
