import type { ReactNode } from "react";
import {
  Calendar,
  Clock,
  Hash,
  Mail,
  MapPin,
  Phone,
  ShoppingBag,
  UtensilsCrossed,
  Users,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { StatusBadge } from "@/components/common/StatusBadge";
import { formatDate, formatDateTime, formatTime } from "@/lib/format";
import { BookingStatus } from "@/types/enums";
import type { BookingResponse } from "@/types/models";
import { CancelBookingDialog } from "./CancelBookingDialog";

// ── Constants ─────────────────────────────────────────────────────────────────

const CANCELLABLE_STATUSES = new Set([BookingStatus.PENDING, BookingStatus.CONFIRMED]);

const TABLE_TYPE_LABELS: Record<string, string> = {
  INDOOR: "Indoor",
  OUTDOOR: "Outdoor / Patio",
  BAR: "Bar",
  PRIVATE: "Private room",
  VIP: "VIP section",
};

// ── Types ─────────────────────────────────────────────────────────────────────

export interface BookingDetailProps {
  booking: BookingResponse;
}

// ── BookingDetail ─────────────────────────────────────────────────────────────

/**
 * Full-detail booking card showing all reservation fields, customer info,
 * assigned table, and linked pre-order (if any).
 *
 * Includes a cancel action for cancellable bookings.
 */
export function BookingDetail({ booking }: BookingDetailProps) {
  const isCancellable = CANCELLABLE_STATUSES.has(booking.status);
  const reservationEnd = booking.endTime ? formatTime(booking.endTime) : null;

  return (
    <Card data-testid="booking-detail">
      {/* Status colour bar */}
      <div className={`h-1.5 w-full rounded-t-lg ${getStatusBarColor(booking.status)}`} />

      <CardHeader className="pb-2">
        <div className="flex items-start justify-between gap-3 flex-wrap">
          <div className="space-y-1">
            <CardTitle className="text-base">{booking.restaurantName}</CardTitle>
            <div className="flex items-center gap-2 flex-wrap">
              <StatusBadge status={booking.status} />
              <span className="text-xs text-muted-foreground font-mono flex items-center gap-1">
                <Hash className="h-3 w-3" aria-hidden />
                {booking.bookingNumber}
              </span>
            </div>
          </div>

          {isCancellable && (
            <CancelBookingDialog
              bookingId={booking.id}
              bookingNumber={booking.bookingNumber}
            />
          )}
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Reservation time */}
        <Section title="Reservation">
          <DetailRow icon={<Calendar className="h-4 w-4" />} label="Date">
            {formatDate(booking.reservationTime)}
          </DetailRow>
          <DetailRow icon={<Clock className="h-4 w-4" />} label="Time">
            {formatTime(booking.reservationTime)}
            {reservationEnd && ` – ${reservationEnd}`}
          </DetailRow>
          <DetailRow icon={<Users className="h-4 w-4" />} label="Party size">
            {booking.partySize} {booking.partySize === 1 ? "guest" : "guests"}
          </DetailRow>
          {booking.tableType && (
            <DetailRow icon={<UtensilsCrossed className="h-4 w-4" />} label="Seating">
              {TABLE_TYPE_LABELS[booking.tableType] ?? booking.tableType}
            </DetailRow>
          )}
          {booking.tableName && (
            <DetailRow icon={<MapPin className="h-4 w-4" />} label="Table">
              {booking.tableName}
            </DetailRow>
          )}
        </Section>

        <Separator />

        {/* Customer info */}
        <Section title="Guest details">
          <DetailRow icon={<Users className="h-4 w-4" />} label="Name">
            {booking.customerName}
          </DetailRow>
          <DetailRow icon={<Phone className="h-4 w-4" />} label="Phone">
            {booking.customerPhone}
          </DetailRow>
          <DetailRow icon={<Mail className="h-4 w-4" />} label="Email">
            {booking.customerEmail}
          </DetailRow>
        </Section>

        {/* Special requests */}
        {booking.specialRequests && (
          <>
            <Separator />
            <Section title="Special requests">
              <p className="text-sm text-muted-foreground italic">
                &ldquo;{booking.specialRequests}&rdquo;
              </p>
            </Section>
          </>
        )}

        {/* Linked pre-order */}
        {booking.preOrderId && (
          <>
            <Separator />
            <Section title="Pre-order">
              <DetailRow icon={<ShoppingBag className="h-4 w-4" />} label="Order ID">
                <span className="font-mono text-xs">{booking.preOrderId}</span>
              </DetailRow>
            </Section>
          </>
        )}

        {/* Timestamps */}
        <Separator />
        <div className="flex gap-4 text-[11px] text-muted-foreground">
          <span>Created {formatDateTime(booking.createdAt)}</span>
          {booking.updatedAt !== booking.createdAt && (
            <span>· Updated {formatDateTime(booking.updatedAt)}</span>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

// ── Internal helpers ──────────────────────────────────────────────────────────

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className="space-y-2">
      <h3 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
        {title}
      </h3>
      {children}
    </div>
  );
}

function DetailRow({
  icon,
  label,
  children,
}: {
  icon: ReactNode;
  label: string;
  children: ReactNode;
}) {
  return (
    <div className="flex items-start gap-2 text-sm">
      <span className="mt-0.5 text-muted-foreground shrink-0" aria-hidden>
        {icon}
      </span>
      <span className="text-muted-foreground w-20 shrink-0">{label}</span>
      <span className="font-medium">{children}</span>
    </div>
  );
}

function getStatusBarColor(status: BookingStatus): string {
  switch (status) {
    case BookingStatus.PENDING:
    case BookingStatus.CONFIRMED:
      return "bg-primary";
    case BookingStatus.SEATED:
      return "bg-amber-500";
    case BookingStatus.COMPLETED:
      return "bg-green-500";
    default:
      return "bg-muted";
  }
}
