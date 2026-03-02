import { useState } from "react";
import { Clock, Users } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { useAvailability } from "@/hooks/use-bookings";
import type { AvailabilityResponse } from "@/types/models";

// ── Types ─────────────────────────────────────────────────────────────────────

export interface AvailabilityCalendarProps {
  /** UUID of the selected restaurant. Required to query availability. */
  restaurantId: string | undefined;
  /** The currently selected ISO datetime string (slot start). */
  selectedSlot: string | null;
  /** Called whenever the user selects a time slot. */
  onSlotChange: (startTime: string) => void;
  /**
   * Optional callback fired when date or partySize changes.
   * Useful for syncing parent form state (e.g. react-hook-form).
   */
  onSearchChange?: (params: { date: string; partySize: number }) => void;
  /** Minimum selectable date (YYYY-MM-DD). Defaults to today. */
  minDate?: string;
}

// ── AvailabilityCalendar ──────────────────────────────────────────────────────

/**
 * Self-contained date picker + party size selector + time slot grid.
 *
 * Manages its own `date` and `partySize` search state. When the user
 * selects a slot, `onSlotChange` is called with the slot's ISO start time.
 *
 * Usage:
 * ```tsx
 * <AvailabilityCalendar
 *   restaurantId={restaurantId}
 *   selectedSlot={selectedSlot}
 *   onSlotChange={setSelectedSlot}
 *   onSearchChange={({ date, partySize }) => {
 *     form.setValue("date", date);
 *     form.setValue("partySize", partySize);
 *   }}
 * />
 * ```
 */
export function AvailabilityCalendar({
  restaurantId,
  selectedSlot,
  onSlotChange,
  onSearchChange,
  minDate,
}: AvailabilityCalendarProps) {
  const today = minDate ?? new Date().toISOString().split("T")[0];

  const [date, setDate] = useState(today);
  const [partySize, setPartySize] = useState(2);

  const canSearch = !!restaurantId && !!date && partySize > 0;

  const { data: availability, isLoading } = useAvailability(
    canSearch ? restaurantId : undefined,
    canSearch ? date : undefined,
    canSearch ? partySize : undefined
  );

  function handleDateChange(newDate: string) {
    setDate(newDate);
    onSlotChange(""); // reset slot on param change
    onSearchChange?.({ date: newDate, partySize });
  }

  function handlePartySizeChange(newSize: number) {
    setPartySize(newSize);
    onSlotChange(""); // reset slot on param change
    onSearchChange?.({ date, partySize: newSize });
  }

  return (
    <div className="space-y-4" data-testid="availability-calendar">
      {/* Search inputs */}
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-1.5">
          <Label htmlFor="av-date">Date *</Label>
          <Input
            id="av-date"
            type="date"
            min={today}
            value={date}
            onChange={(e) => handleDateChange(e.target.value)}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="av-party-size">Party size *</Label>
          <Input
            id="av-party-size"
            type="number"
            min={1}
            max={20}
            value={partySize}
            onChange={(e) => handlePartySizeChange(Math.max(1, Number(e.target.value)))}
          />
        </div>
      </div>

      {/* Slot grid */}
      {canSearch && (
        <SlotGrid
          isLoading={isLoading}
          availability={availability}
          selectedSlot={selectedSlot}
          onSelect={onSlotChange}
        />
      )}
    </div>
  );
}

// ── SlotGrid ─────────────────────────────────────────────────────────────────

export interface SlotGridProps {
  isLoading: boolean;
  availability: AvailabilityResponse | undefined;
  selectedSlot: string | null;
  onSelect: (startTime: string) => void;
}

export function SlotGrid({ isLoading, availability, selectedSlot, onSelect }: SlotGridProps) {
  if (isLoading) {
    return (
      <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
        {Array.from({ length: 8 }).map((_, i) => (
          // biome-ignore lint/suspicious/noArrayIndexKey: skeleton placeholders
          <Skeleton key={i} className="h-14 w-full rounded-md" />
        ))}
      </div>
    );
  }

  if (availability?.fullyBooked) {
    return (
      <p className="rounded-md border border-muted bg-muted/30 py-6 text-center text-sm text-muted-foreground">
        No tables available on this date. Try a different date or party size.
      </p>
    );
  }

  if (!availability?.availableSlots?.length) {
    return (
      <p className="rounded-md border border-muted bg-muted/30 py-6 text-center text-sm text-muted-foreground">
        No slots found. Adjust your search criteria.
      </p>
    );
  }

  return (
    <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
      {availability.availableSlots.map((slot) => {
        const slotTime = new Date(slot.startTime).toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
        });
        const isSelected = selectedSlot === slot.startTime;
        return (
          <button
            key={slot.startTime}
            type="button"
            onClick={() => onSelect(slot.startTime)}
            className={`flex flex-col items-center justify-center gap-0.5 rounded-md border px-3 py-3 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${
              isSelected
                ? "border-primary bg-primary text-primary-foreground"
                : "border-input bg-background hover:bg-accent hover:text-accent-foreground"
            }`}
          >
            <Clock className="h-3.5 w-3.5 shrink-0" aria-hidden />
            <span>{slotTime}</span>
            <span className="text-[10px] opacity-70">
              <Users className="inline h-3 w-3 mr-0.5" />
              {slot.availableCapacity}
            </span>
          </button>
        );
      })}
    </div>
  );
}
