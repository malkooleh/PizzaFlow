import { Link } from "@tanstack/react-router";
import { CalendarPlus } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { BookingStatus } from "@/types/enums";
import type { BookingResponse } from "@/types/models";
import { useBookings } from "@/hooks/use-bookings";
import { BookingCard } from "./BookingCard";

const UPCOMING_STATUSES = new Set([
  BookingStatus.PENDING,
  BookingStatus.CONFIRMED,
  BookingStatus.SEATED,
]);

const PAST_STATUSES = new Set([BookingStatus.COMPLETED, BookingStatus.NO_SHOW]);

interface BookingListProps {
  /** Keycloak UUID subject — NOT the numeric order-service customer ID */
  customerId: string | undefined;
}

export function BookingList({ customerId }: BookingListProps) {
  const { data: bookings = [], isLoading, error } = useBookings(customerId);

  if (isLoading) return <BookingListSkeleton />;

  if (error) {
    return (
      <div className="rounded-lg border border-destructive/30 bg-destructive/5 p-6 text-center">
        <p className="text-destructive font-medium">Failed to load bookings.</p>
        <p className="text-sm text-muted-foreground mt-1">
          Please refresh the page to try again.
        </p>
      </div>
    );
  }

  const upcoming = bookings.filter((b) => UPCOMING_STATUSES.has(b.status));
  const past = bookings.filter((b) => PAST_STATUSES.has(b.status));
  const cancelled = bookings.filter((b) => b.status === BookingStatus.CANCELLED);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">My Bookings</h1>
        <Button asChild size="sm" className="gap-1.5">
          <Link to="/bookings/new">
            <CalendarPlus className="h-4 w-4" />
            Book a table
          </Link>
        </Button>
      </div>

      {bookings.length === 0 ? (
        <EmptyBookings />
      ) : (
        <Tabs defaultValue="upcoming">
          <TabsList className="mb-4">
            <TabsTrigger value="upcoming">
              Upcoming
              {upcoming.length > 0 && (
                <span className="ml-1.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-primary px-1 text-[10px] font-semibold text-primary-foreground">
                  {upcoming.length}
                </span>
              )}
            </TabsTrigger>
            <TabsTrigger value="past">Past</TabsTrigger>
            <TabsTrigger value="cancelled">Cancelled</TabsTrigger>
          </TabsList>

          <TabsContent value="upcoming">
            <BookingSection bookings={upcoming} emptyMessage="No upcoming bookings." />
          </TabsContent>

          <TabsContent value="past">
            <BookingSection bookings={past} emptyMessage="No past bookings." />
          </TabsContent>

          <TabsContent value="cancelled">
            <BookingSection bookings={cancelled} emptyMessage="No cancelled bookings." />
          </TabsContent>
        </Tabs>
      )}
    </div>
  );
}

function BookingSection({
  bookings,
  emptyMessage,
}: {
  bookings: BookingResponse[];
  emptyMessage: string;
}) {
  if (bookings.length === 0) {
    return (
      <p className="py-8 text-center text-sm text-muted-foreground">{emptyMessage}</p>
    );
  }
  return (
    <div className="space-y-3">
      {bookings.map((b) => (
        <BookingCard key={b.id} booking={b} />
      ))}
    </div>
  );
}

function EmptyBookings() {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center gap-4">
      <div className="rounded-full bg-muted p-5">
        <CalendarPlus className="h-8 w-8 text-muted-foreground" />
      </div>
      <div>
        <p className="font-semibold">No reservations yet</p>
        <p className="text-sm text-muted-foreground mt-1">
          Reserve a table for your next pizza night.
        </p>
      </div>
      <Button asChild>
        <Link to="/bookings/new">Book a table</Link>
      </Button>
    </div>
  );
}

function BookingListSkeleton() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Skeleton className="h-7 w-36" />
        <Skeleton className="h-9 w-28" />
      </div>
      <div className="space-y-3">
        {[1, 2, 3].map((n) => (
          <div key={n} className="rounded-lg border p-5 space-y-2">
            <Skeleton className="h-5 w-48" />
            <Skeleton className="h-4 w-36" />
            <Skeleton className="h-4 w-24" />
          </div>
        ))}
      </div>
    </div>
  );
}
