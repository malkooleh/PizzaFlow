import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";

const schema = z.object({
  street: z.string().min(3, "Street is required"),
  city: z.string().min(2, "City is required"),
  zipCode: z.string().min(2, "Zip code is required"),
  district: z.string().optional(),
  additionalInfo: z.string().optional(),
});

export type DeliveryAddressValues = z.infer<typeof schema>;

interface DeliveryAddressFormProps {
  defaultValues?: Partial<DeliveryAddressValues>;
  onSubmit: (values: DeliveryAddressValues) => void;
  submitLabel?: string;
}

export function DeliveryAddressForm({
  defaultValues,
  onSubmit,
  submitLabel = "Continue",
}: DeliveryAddressFormProps) {
  const form = useForm<DeliveryAddressValues>({
    resolver: zodResolver(schema),
    defaultValues: { street: "", city: "", zipCode: "", district: "", additionalInfo: "", ...defaultValues },
  });

  return (
    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
      <div className="space-y-1.5">
        <Label htmlFor="street">Street address *</Label>
        <Input id="street" {...form.register("street")} placeholder="123 Main St" />
        {form.formState.errors.street && (
          <p className="text-xs text-destructive">{form.formState.errors.street.message}</p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="city">City *</Label>
          <Input id="city" {...form.register("city")} placeholder="New York" />
          {form.formState.errors.city && (
            <p className="text-xs text-destructive">{form.formState.errors.city.message}</p>
          )}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="zipCode">ZIP code *</Label>
          <Input id="zipCode" {...form.register("zipCode")} placeholder="10001" />
          {form.formState.errors.zipCode && (
            <p className="text-xs text-destructive">{form.formState.errors.zipCode.message}</p>
          )}
        </div>
      </div>

      <div className="space-y-1.5">
        <Label htmlFor="district">District / Neighborhood</Label>
        <Input id="district" {...form.register("district")} placeholder="Optional" />
      </div>

      <div className="space-y-1.5">
        <Label htmlFor="additionalInfo">Additional info</Label>
        <Input
          id="additionalInfo"
          {...form.register("additionalInfo")}
          placeholder="Apt, floor, buzzer code…"
        />
      </div>

      <Button type="submit" className="w-full">
        {submitLabel}
      </Button>
    </form>
  );
}
