import { useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { BookingForm } from "@/features/bookings/components/BookingForm";
import { BookingConfirmation } from "@/features/bookings/components/BookingConfirmation";
import type { BookingResponse } from "@/types/models";

export const Route = createFileRoute("/bookings/new")({
  component: NewBookingRoute,
});

function NewBookingRoute() {
  const [confirmedBooking, setConfirmedBooking] = useState<BookingResponse | null>(null);

  return (
    <ProtectedRoute>
      <div className="container mx-auto max-w-xl px-4 py-8">
        {confirmedBooking ? (
          <BookingConfirmation booking={confirmedBooking} />
        ) : (
          <>
            <div className="mb-6">
              <h1 className="text-xl font-bold">Reserve a table</h1>
              <p className="text-sm text-muted-foreground mt-1">
                Check availability and make a reservation in seconds.
              </p>
            </div>
            <BookingForm onSuccess={setConfirmedBooking} />
          </>
        )}
      </div>
    </ProtectedRoute>
  );
}

