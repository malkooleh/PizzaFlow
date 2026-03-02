import { CheckCircle2, Calendar, Users, Hash, ArrowRight, UtensilsCrossed } from "lucide-react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { formatDateTime } from "@/lib/format";
import type { BookingResponse } from "@/types/models";

interface BookingConfirmationProps {
  booking: BookingResponse;
}

export function BookingConfirmation({ booking }: BookingConfirmationProps) {
  return (
    <div className="flex flex-col items-center text-center space-y-6 py-6">
      {/* Success icon */}
      <div className="rounded-full bg-green-100 p-5 dark:bg-green-900/20">
        <CheckCircle2 className="h-12 w-12 text-green-600 dark:text-green-400" />
      </div>

      <div className="space-y-1">
        <h1 className="text-2xl font-bold">Reservation confirmed!</h1>
        <p className="text-muted-foreground text-sm">
          We&rsquo;ll see you soon at {booking.restaurantName}.
        </p>
      </div>

      {/* Booking number pill */}
      <div className="rounded-full bg-muted px-5 py-2 flex items-center gap-2">
        <Hash className="h-4 w-4 text-muted-foreground shrink-0" />
        <span className="font-mono font-semibold text-sm">{booking.bookingNumber}</span>
      </div>

      <Separator className="w-full max-w-sm" />

      {/* Booking details */}
      <dl className="w-full max-w-sm text-left space-y-3 text-sm">
        <div className="flex justify-between">
          <dt className="text-muted-foreground flex items-center gap-1.5">
            <Calendar className="h-4 w-4" />
            Date &amp; time
          </dt>
          <dd className="font-medium">{formatDateTime(booking.reservationTime)}</dd>
        </div>

        <div className="flex justify-between">
          <dt className="text-muted-foreground flex items-center gap-1.5">
            <Users className="h-4 w-4" />
            Party size
          </dt>
          <dd className="font-medium">
            {booking.partySize} {booking.partySize === 1 ? "guest" : "guests"}
          </dd>
        </div>

        {booking.tableName && (
          <div className="flex justify-between">
            <dt className="text-muted-foreground flex items-center gap-1.5">
              <UtensilsCrossed className="h-4 w-4" />
              Table
            </dt>
            <dd className="font-medium">{booking.tableName}</dd>
          </div>
        )}

        <div className="flex justify-between">
          <dt className="text-muted-foreground">Guest name</dt>
          <dd className="font-medium">{booking.customerName}</dd>
        </div>

        {booking.specialRequests && (
          <div className="flex justify-between gap-4">
            <dt className="text-muted-foreground shrink-0">Notes</dt>
            <dd className="text-right italic">{booking.specialRequests}</dd>
          </div>
        )}
      </dl>

      <Separator className="w-full max-w-sm" />

      <div className="flex flex-col sm:flex-row gap-3 w-full max-w-sm">
        <Button variant="outline" asChild className="flex-1">
          <Link to="/bookings">View my bookings</Link>
        </Button>
        <Button asChild className="flex-1 gap-1.5">
          <Link to="/menu">
            Browse menu
            <ArrowRight className="h-4 w-4" />
          </Link>
        </Button>
      </div>
    </div>
  );
}
