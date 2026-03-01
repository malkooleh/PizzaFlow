import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface TableNumberInputProps {
  value: string;
  onChange: (value: string) => void;
}

export function TableNumberInput({ value, onChange }: TableNumberInputProps) {
  return (
    <div className="space-y-1.5">
      <Label htmlFor="tableNumber">Table number *</Label>
      <Input
        id="tableNumber"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="e.g. 7"
        className="w-32"
        inputMode="numeric"
        pattern="[0-9]*"
      />
      <p className="text-xs text-muted-foreground">
        Find the table number on the QR code stand at your table.
      </p>
    </div>
  );
}
