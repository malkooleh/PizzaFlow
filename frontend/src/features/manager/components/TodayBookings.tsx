import { Clock, Users, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/common/StatusBadge";
import { formatTime } from "@/lib/format";
import {
  useConfirmBooking,
  useNoShowBooking,
  useSeatBooking,
  useTodayBookings,
} from "@/hooks/use-manager";
import { BookingStatus } from "@/types/enums";
import type { BookingResponse } from "@/types/models";

interface TodayBookingsProps {
  restaurantId: string;
}

interface BookingActionsProps {
  booking: BookingResponse;
  restaurantId: string;
}

function BookingActions({ booking, restaurantId }: BookingActionsProps) {
  const confirm = useConfirmBooking(restaurantId);
  const seat = useSeatBooking(restaurantId);
  const noShow = useNoShowBooking(restaurantId);

  if (booking.status === BookingStatus.PENDING) {
    return (
      <Button
        size="sm"
        disabled={confirm.isPending}
        onClick={() => confirm.mutate(booking.id)}
      >
        {confirm.isPending && (
          <Loader2 className="mr-1.5 h-3 w-3 animate-spin" />
        )}
        Confirm
      </Button>
    );
  }

  if (booking.status === BookingStatus.CONFIRMED) {
    return (
      <div className="flex gap-1.5">
        <Button
          size="sm"
          disabled={seat.isPending}
          onClick={() => seat.mutate(booking.id)}
        >
          {seat.isPending && <Loader2 className="mr-1.5 h-3 w-3 animate-spin" />}
          Seat
        </Button>
        <Button
          size="sm"
          variant="outline"
          disabled={noShow.isPending}
          onClick={() => noShow.mutate(booking.id)}
        >
          No Show
        </Button>
      </div>
    );
  }

  return null;
}

export function TodayBookings({ restaurantId }: TodayBookingsProps) {
  const { data: bookings, isLoading } = useTodayBookings(restaurantId);

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 4 }).map((_, i) => (
          <Skeleton key={i} className="h-16 w-full" />
        ))}
      </div>
    );
  }

  if (!bookings?.length) {
    return (
      <div className="py-10 text-center text-muted-foreground">
        No bookings scheduled for today.
      </div>
    );
  }

  const sorted = [...bookings].sort((a, b) =>
    a.reservationTime.localeCompare(b.reservationTime),
  );

  return (
    <div className="space-y-2">
      {sorted.map((booking) => (
        <Card key={booking.id}>
          <CardContent className="flex items-center justify-between gap-3 p-3">
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-2">
                <p className="font-medium">{booking.customerName}</p>
                <StatusBadge status={booking.status} className="text-xs" />
              </div>
              <div className="mt-1 flex items-center gap-3 text-xs text-muted-foreground">
                <span className="flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  {formatTime(booking.reservationTime)}
                </span>
                <span className="flex items-center gap-1">
                  <Users className="h-3 w-3" />
                  {booking.partySize} guests
                </span>
                <span>{booking.customerPhone}</span>
              </div>
            </div>
            <BookingActions booking={booking} restaurantId={restaurantId} />
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
