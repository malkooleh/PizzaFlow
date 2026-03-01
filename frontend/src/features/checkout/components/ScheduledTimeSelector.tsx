import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface ScheduledTimeSelectorProps {
  value: string;
  onChange: (isoDateTime: string) => void;
}

/** Produces a datetime-local input floored to minutes, minimum = now + 30 min. */
export function ScheduledTimeSelector({ value, onChange }: ScheduledTimeSelectorProps) {
  const minDate = new Date(Date.now() + 30 * 60_000);
  const pad = (n: number) => String(n).padStart(2, "0");
  const minValue = `${minDate.getFullYear()}-${pad(minDate.getMonth() + 1)}-${pad(minDate.getDate())}T${pad(minDate.getHours())}:${pad(minDate.getMinutes())}`;

  return (
    <div className="space-y-1.5">
      <Label htmlFor="scheduledTime">Schedule for *</Label>
      <Input
        id="scheduledTime"
        type="datetime-local"
        value={value}
        min={minValue}
        onChange={(e) => onChange(e.target.value ? new Date(e.target.value).toISOString() : "")}
        className="w-64"
      />
      <p className="text-xs text-muted-foreground">Minimum 30 minutes from now.</p>
    </div>
  );
}
