import { format } from "date-fns";
import { CalendarIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface DateRangeFilterProps {
  from: string;
  to: string;
  onFromChange: (value: string) => void;
  onToChange: (value: string) => void;
}

export function DateRangeFilter({
  from,
  to,
  onFromChange,
  onToChange,
}: DateRangeFilterProps) {
  function handlePreset(days: number) {
    const end = format(new Date(), "yyyy-MM-dd");
    const start = format(
      new Date(Date.now() - days * 24 * 60 * 60 * 1000),
      "yyyy-MM-dd",
    );
    onFromChange(start);
    onToChange(end);
  }

  return (
    <div className="flex flex-wrap items-end gap-3">
      <div>
        <Label className="text-xs">From</Label>
        <Input
          type="date"
          value={from}
          max={to}
          onChange={(e) => onFromChange(e.target.value)}
          className="mt-1 w-36"
        />
      </div>
      <div>
        <Label className="text-xs">To</Label>
        <Input
          type="date"
          value={to}
          min={from}
          max={format(new Date(), "yyyy-MM-dd")}
          onChange={(e) => onToChange(e.target.value)}
          className="mt-1 w-36"
        />
      </div>
      <div className="flex gap-1.5">
        <Button size="sm" variant="outline" onClick={() => handlePreset(7)}>
          7 days
        </Button>
        <Button size="sm" variant="outline" onClick={() => handlePreset(14)}>
          14 days
        </Button>
        <Button size="sm" variant="outline" onClick={() => handlePreset(30)}>
          30 days
        </Button>
      </div>
    </div>
  );
}
