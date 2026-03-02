import { Calendar, Users, UtensilsCrossed, Hash } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { StatusBadge } from "@/components/common/StatusBadge";
import { formatDateTime } from "@/lib/format";
import { BookingStatus } from "@/types/enums";
import type { BookingResponse } from "@/types/models";
import { CancelBookingDialog } from "./CancelBookingDialog";

const CANCELLABLE_STATUSES = new Set([BookingStatus.PENDING, BookingStatus.CONFIRMED]);

interface BookingCardProps {
  booking: BookingResponse;
}

export function BookingCard({ booking }: BookingCardProps) {
  const isCancellable = CANCELLABLE_STATUSES.has(booking.status);
  const reservationDate = new Date(booking.reservationTime);
  const isUpcoming = reservationDate > new Date();

  return (
    <Card className="overflow-hidden">
      <div className={`h-1 w-full ${getStatusBarColor(booking.status, isCancellable)}`} />
      <CardContent className="pt-4 pb-4 px-5">
        <div className="flex items-start justify-between gap-3 flex-wrap">
          {/* Left: booking details */}
          <div className="space-y-2 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="font-semibold text-sm">{booking.restaurantName}</span>
              <StatusBadge status={booking.status} />
              {isUpcoming && isCancellable && (
                <span className="flex h-2 w-2 rounded-full bg-primary animate-pulse" aria-hidden />
              )}
            </div>

            <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
              <Calendar className="h-3.5 w-3.5 shrink-0" />
              <span>{formatDateTime(booking.reservationTime)}</span>
            </div>

            <div className="flex items-center gap-4 text-xs text-muted-foreground">
              <span className="flex items-center gap-1">
                <Users className="h-3.5 w-3.5" />
                {booking.partySize} {booking.partySize === 1 ? "guest" : "guests"}
              </span>

              {booking.tableType && (
                <span className="flex items-center gap-1">
                  <UtensilsCrossed className="h-3.5 w-3.5" />
                  {TABLE_TYPE_LABELS[booking.tableType] ?? booking.tableType}
                </span>
              )}

              <span className="flex items-center gap-1 font-mono">
                <Hash className="h-3.5 w-3.5" />
                {booking.bookingNumber}
              </span>
            </div>

            {booking.specialRequests && (
              <p className="text-xs text-muted-foreground italic">
                &ldquo;{booking.specialRequests}&rdquo;
              </p>
            )}
          </div>

          {/* Right: action */}
          {isCancellable && (
            <div className="shrink-0">
              <CancelBookingDialog
                bookingId={booking.id}
                bookingNumber={booking.bookingNumber}
              />
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

function getStatusBarColor(status: BookingStatus, isCancellable: boolean): string {
  if (isCancellable) return "bg-primary";
  if (status === BookingStatus.COMPLETED) return "bg-green-500";
  if (status === BookingStatus.CANCELLED || status === BookingStatus.NO_SHOW) return "bg-muted";
  return "bg-amber-500";
}

const TABLE_TYPE_LABELS: Record<string, string> = {
  INDOOR: "Indoor",
  OUTDOOR: "Outdoor",
  BAR: "Bar",
  PRIVATE: "Private",
  VIP: "VIP",
};
