import { useAuth } from "react-oidc-context";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import { usePreferences, useUpdatePreferences } from "@/hooks/use-notifications";
import type { NotificationPreference } from "@/types/models";

// ── Toggle switch primitive ──────────────────────────────────────────────────

interface ToggleSwitchProps {
  readonly checked: boolean;
  readonly onToggle: () => void;
  readonly label: string;
  readonly description?: string;
  readonly disabled?: boolean;
}

function ToggleSwitch({
  checked,
  onToggle,
  label,
  description,
  disabled = false,
}: Readonly<ToggleSwitchProps>) {
  return (
    <div className="flex items-center justify-between gap-4 py-3">
      <div className="space-y-0.5">
        <p className="text-sm font-medium leading-none">{label}</p>
        {description && <p className="text-xs text-muted-foreground">{description}</p>}
      </div>
      <button
        role="switch"
        aria-checked={checked}
        aria-label={label}
        disabled={disabled}
        onClick={onToggle}
        className={cn(
          "relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
          checked ? "bg-primary" : "bg-input",
        )}
      >
        <span
          className={cn(
            "pointer-events-none block h-5 w-5 rounded-full bg-background shadow-lg ring-0 transition-transform",
            checked ? "translate-x-5" : "translate-x-0",
          )}
        />
      </button>
    </div>
  );
}

// ── Preferences section wrapper ──────────────────────────────────────────────

function PreferencesSection({
  title,
  children,
}: Readonly<{ title: string; children: React.ReactNode }>) {
  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent className="divide-y px-6">
        {children}
      </CardContent>
    </Card>
  );
}

// ── Main component ────────────────────────────────────────────────────────────

export function NotificationPreferences() {
  const auth = useAuth();
  const userId = auth.user?.profile.sub;

  const { data: prefs, isLoading } = usePreferences(userId);
  const updatePreferences = useUpdatePreferences(userId);

  const toggle = (field: keyof NotificationPreference) => {
    if (!prefs) return;
    updatePreferences.mutate({ [field]: !prefs[field] });
  };

  if (isLoading || !prefs) {
    return (
      <div className="space-y-4 max-w-xl">
        {[1, 2, 3].map((i) => (
          <Card key={i}>
            <CardHeader className="pb-2">
              <Skeleton className="h-5 w-32" />
            </CardHeader>
            <CardContent className="space-y-4 px-6">
              {[1, 2, 3].map((j) => (
                <div key={j} className="flex items-center justify-between py-1">
                  <Skeleton className="h-4 w-40" />
                  <Skeleton className="h-6 w-11 rounded-full" />
                </div>
              ))}
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  const isPending = updatePreferences.isPending;

  return (
    <div className="space-y-6 max-w-xl">
      {/* Delivery channels */}
      <PreferencesSection title="Notification Channels">
        <ToggleSwitch
          checked={prefs.inAppEnabled}
          onToggle={() => toggle("inAppEnabled")}
          label="In-App"
          description="Show notifications inside PizzaFlow"
          disabled={isPending}
        />
        <ToggleSwitch
          checked={prefs.emailEnabled}
          onToggle={() => toggle("emailEnabled")}
          label="Email"
          description="Receive notifications via email"
          disabled={isPending}
        />
        <ToggleSwitch
          checked={prefs.smsEnabled}
          onToggle={() => toggle("smsEnabled")}
          label="SMS"
          description="Receive text message notifications"
          disabled={isPending}
        />
        <ToggleSwitch
          checked={prefs.pushEnabled}
          onToggle={() => toggle("pushEnabled")}
          label="Push"
          description="Browser push notifications"
          disabled={isPending}
        />
      </PreferencesSection>

      {/* Topic categories */}
      <PreferencesSection title="Notification Topics">
        <ToggleSwitch
          checked={prefs.orderUpdates}
          onToggle={() => toggle("orderUpdates")}
          label="Order Updates"
          description="Status changes, confirmations, cancellations"
          disabled={isPending}
        />
        <ToggleSwitch
          checked={prefs.paymentNotifications}
          onToggle={() => toggle("paymentNotifications")}
          label="Payments"
          description="Payment confirmations and failures"
          disabled={isPending}
        />
        <ToggleSwitch
          checked={prefs.deliveryTracking}
          onToggle={() => toggle("deliveryTracking")}
          label="Delivery Tracking"
          description="Courier assigned, en-route, delivered"
          disabled={isPending}
        />
        <ToggleSwitch
          checked={prefs.bookingReminders}
          onToggle={() => toggle("bookingReminders")}
          label="Booking Reminders"
          description="Upcoming table reservation reminders"
          disabled={isPending}
        />
        <ToggleSwitch
          checked={prefs.promotionalMessages}
          onToggle={() => toggle("promotionalMessages")}
          label="Promotions"
          description="Special offers and discounts"
          disabled={isPending}
        />
      </PreferencesSection>

      {/* Quiet hours */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-base">Quiet Hours</CardTitle>
        </CardHeader>
        <CardContent className="px-6 pb-4">
          <p className="text-xs text-muted-foreground mb-4">
            Suppress non-urgent notifications during these hours.
          </p>
          <div className="flex items-center gap-4">
            <div className="space-y-1">
              <label htmlFor="quiet-start" className="text-sm font-medium">
                From
              </label>
              <input
                id="quiet-start"
                type="time"
                value={prefs.quietHoursStart ?? ""}
                onChange={(e) => updatePreferences.mutate({ quietHoursStart: e.target.value || undefined })}
                disabled={isPending}
                className="flex h-9 w-32 rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
              />
            </div>
            <Separator orientation="vertical" className="h-9" />
            <div className="space-y-1">
              <label htmlFor="quiet-end" className="text-sm font-medium">
                To
              </label>
              <input
                id="quiet-end"
                type="time"
                value={prefs.quietHoursEnd ?? ""}
                onChange={(e) => updatePreferences.mutate({ quietHoursEnd: e.target.value || undefined })}
                disabled={isPending}
                className="flex h-9 w-32 rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
              />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
