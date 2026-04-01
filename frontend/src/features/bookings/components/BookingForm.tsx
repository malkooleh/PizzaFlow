import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAuth } from "react-oidc-context";
import { ChevronRight, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import { Separator } from "@/components/ui/separator";
import { useBookingRestaurants, useCreateBooking } from "@/hooks/use-bookings";
import type { CreateBookingRequest } from "@/api/bookings.api";
import type { BookingResponse } from "@/types/models";
import type { TableType } from "@/types/enums";
import { AvailabilityCalendar } from "./AvailabilityCalendar";
import { TableTypePreference } from "./TableTypePreference";

// ── Zod schema ───────────────────────────────────────────────────────────────

const schema = z.object({
  restaurantId: z.string().min(1, "Please select a restaurant"),
  partySize: z.coerce.number().int().min(1, "At least 1 guest").max(20, "Maximum 20 guests"),
  customerName: z.string().min(2, "Name is required"),
  customerPhone: z.string().min(7, "Phone number is required"),
  customerEmail: z.string().email("Valid email address is required"),
  preferredTableType: z.string().optional(),
  specialRequests: z.string().max(500).optional(),
});

type BookingFormValues = z.infer<typeof schema>;

// ── Component ────────────────────────────────────────────────────────────────

interface BookingFormProps {
  onSuccess: (booking: BookingResponse) => void;
}

export function BookingForm({ onSuccess }: BookingFormProps) {
  const auth = useAuth();
  const [selectedSlot, setSelectedSlot] = useState<string | null>(null);
  const [slotError, setSlotError] = useState(false);

  const { data: restaurants = [], isLoading: restaurantsLoading } = useBookingRestaurants();
  const createBooking = useCreateBooking();

  const form = useForm<BookingFormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      restaurantId: "",
      partySize: 2,
      customerName: auth.user?.profile.name ?? "",
      customerEmail: auth.user?.profile.email ?? "",
      customerPhone: "",
      preferredTableType: "",
      specialRequests: "",
    },
  });

  const watchRestaurantId = form.watch("restaurantId");

  async function handleSubmit(values: BookingFormValues) {
    if (!selectedSlot) {
      setSlotError(true);
      return;
    }
    setSlotError(false);

    const customerId = auth.user?.profile.sub;
    if (!customerId) return;

    const request: CreateBookingRequest = {
      customerId,
      restaurantId: values.restaurantId,
      reservationTime: selectedSlot,
      partySize: values.partySize,
      customerName: values.customerName,
      customerPhone: values.customerPhone,
      customerEmail: values.customerEmail,
      specialRequests: values.specialRequests?.trim() || undefined,
      preferredTableType: (values.preferredTableType as TableType) || undefined,
    };

    const booking = await createBooking.mutateAsync(request);
    onSuccess(booking);
  }

  return (
    <form onSubmit={(e) => { void form.handleSubmit(handleSubmit)(e); }} className="space-y-8">
      {/* ── Section 1: Search ── */}
      <section className="space-y-4">
        <SectionLabel step={1} title="Find a table" />

        <div className="space-y-1.5">
          <Label htmlFor="restaurantId">Restaurant *</Label>
          {restaurantsLoading ? (
            <Skeleton className="h-10 w-full" />
          ) : (
            <select
              id="restaurantId"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:opacity-50"
              {...form.register("restaurantId", {
                onChange: () => setSelectedSlot(null),
              })}
            >
              <option value="">Select a restaurant…</option>
              {restaurants.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.name} — {r.address}
                </option>
              ))}
            </select>
          )}
          {form.formState.errors.restaurantId && (
            <p className="text-xs text-destructive">
              {form.formState.errors.restaurantId.message}
            </p>
          )}
        </div>

        {/* Availability calendar — date picker + slot grid */}
        <AvailabilityCalendar
          restaurantId={watchRestaurantId || undefined}
          selectedSlot={selectedSlot}
          onSlotChange={(slot) => {
            setSelectedSlot(slot || null);
            setSlotError(false);
          }}
          onSearchChange={({ partySize }) => {
            form.setValue("partySize", partySize, { shouldValidate: true });
          }}
        />

        {slotError && (
          <p className="text-xs text-destructive">Please select a time slot to continue.</p>
        )}
      </section>

      {/* ── Section 2: Contact & Preferences ── */}
      <Separator />
      <section className="space-y-4">
        <SectionLabel step={2} title="Your details" />

        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-1.5">
            <Label htmlFor="customerName">Full name *</Label>
            <Input
              id="customerName"
              placeholder="Jane Smith"
              {...form.register("customerName")}
            />
            {form.formState.errors.customerName && (
              <p className="text-xs text-destructive">
                {form.formState.errors.customerName.message}
              </p>
            )}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="customerPhone">Phone *</Label>
            <Input
              id="customerPhone"
              type="tel"
              placeholder="+1 555 000 0000"
              {...form.register("customerPhone")}
            />
            {form.formState.errors.customerPhone && (
              <p className="text-xs text-destructive">
                {form.formState.errors.customerPhone.message}
              </p>
            )}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="customerEmail">Email *</Label>
            <Input
              id="customerEmail"
              type="email"
              placeholder="jane@example.com"
              {...form.register("customerEmail")}
            />
            {form.formState.errors.customerEmail && (
              <p className="text-xs text-destructive">
                {form.formState.errors.customerEmail.message}
              </p>
            )}
          </div>

          <TableTypePreference
            id="preferredTableType"
            value={form.watch("preferredTableType") ?? ""}
            onChange={(val) => form.setValue("preferredTableType", val)}
          />
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="specialRequests">
            Special requests{" "}
            <span className="text-muted-foreground text-xs">(optional · max 500 chars)</span>
          </Label>
          <Textarea
            id="specialRequests"
            placeholder="e.g. Window seat, birthday cake, high chair needed, wheelchair access…"
            rows={3}
            maxLength={500}
            {...form.register("specialRequests")}
          />
        </div>
      </section>

      {/* ── Submit ── */}
      <Button
        type="submit"
        className="w-full gap-2"
        size="lg"
        disabled={createBooking.isPending || !auth.user}
      >
        {createBooking.isPending ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin" />
            Booking table…
          </>
        ) : (
          <>
            Confirm reservation
            <ChevronRight className="h-4 w-4" />
          </>
        )}
      </Button>

      {createBooking.isError && (
        <p className="text-sm text-destructive text-center mt-2">
          {createBooking.error?.message ?? "Booking failed. Please try again."}
        </p>
      )}
    </form>
  );
}

// ── SectionLabel ─────────────────────────────────────────────────────────────

function SectionLabel({ step, title }: { step: number; title: string }) {
  return (
    <div className="flex items-center gap-2">
      <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-bold text-primary-foreground">
        {step}
      </span>
      <h2 className="font-semibold">{title}</h2>
    </div>
  );
}
