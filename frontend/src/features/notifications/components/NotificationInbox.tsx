import { formatRelativeTime } from "@/lib/format";
import { BellOff, CheckCheck, ExternalLink, ShoppingCart, CreditCard, Package, Calendar, Truck, AlertTriangle, Info } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import type { InAppNotificationResponse } from "@/types/models";

// ── Icon mapping by eventType ─────────────────────────────────────────────────

const EVENT_ICON: Record<string, React.ReactNode> = {
  ORDER_CREATED: <ShoppingCart className="h-4 w-4" />,
  ORDER_STATUS_CHANGED: <ShoppingCart className="h-4 w-4" />,
  PAYMENT_PROCESSED: <CreditCard className="h-4 w-4" />,
  PAYMENT_FAILED: <CreditCard className="h-4 w-4" />,
  BOOKING_CONFIRMED: <Calendar className="h-4 w-4" />,
  BOOKING_CANCELLED: <Calendar className="h-4 w-4" />,
  DELIVERY_UPDATE: <Truck className="h-4 w-4" />,
  LOW_STOCK_ALERT: <Package className="h-4 w-4" />,
  SYSTEM_ALERT: <AlertTriangle className="h-4 w-4" />,
};

function getEventIcon(eventType: string): React.ReactNode {
  return EVENT_ICON[eventType] ?? <Info className="h-4 w-4" />;
}

// ── Notification item ─────────────────────────────────────────────────────────

interface NotificationItemProps {
  readonly notification: InAppNotificationResponse;
  readonly onMarkRead: (id: string) => void;
}

function NotificationItem({ notification, onMarkRead }: Readonly<NotificationItemProps>) {
  const handleClick = () => {
    if (!notification.isRead) {
      onMarkRead(notification.id);
    }
  };

  return (
    <button
      type="button"
      className={cn(
        "flex w-full gap-3 px-4 py-3 text-left hover:bg-muted/50 transition-colors",
        !notification.isRead && "bg-primary/5",
      )}
      onClick={handleClick}
    >
      {/* Icon */}
      <div
        className={cn(
          "mt-0.5 shrink-0 rounded-full p-1.5",
          notification.isRead ? "text-muted-foreground bg-muted" : "text-primary bg-primary/10",
        )}
      >
        {getEventIcon(notification.eventType)}
      </div>

      {/* Content */}
      <div className="min-w-0 flex-1">
        <p className={cn("text-sm leading-snug", !notification.isRead && "font-medium")}>
          {notification.title}
        </p>
        <p className="text-xs text-muted-foreground mt-0.5 line-clamp-2">{notification.message}</p>
        <p className="text-xs text-muted-foreground/70 mt-1">
          {formatRelativeTime(notification.createdAt)}
        </p>
      </div>

      {/* Unread dot */}
      {!notification.isRead && (
        <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-primary" />
      )}

      {/* Action link */}
      {notification.actionUrl && (
        <a
          href={notification.actionUrl}
          onClick={(e) => e.stopPropagation()}
          className="mt-1 shrink-0 text-muted-foreground hover:text-foreground"
          aria-label="Open"
        >
          <ExternalLink className="h-3.5 w-3.5" />
        </a>
      )}
    </button>
  );
}

// ── Main component ────────────────────────────────────────────────────────────

interface NotificationInboxProps {
  readonly notifications: InAppNotificationResponse[];
  readonly onMarkRead: (id: string) => void;
  readonly onMarkAllRead: () => void;
  readonly isMarkingAll: boolean;
}

export function NotificationInbox({
  notifications,
  onMarkRead,
  onMarkAllRead,
  isMarkingAll,
}: Readonly<NotificationInboxProps>) {
  const unreadCount = notifications.filter((n) => !n.isRead).length;

  if (notifications.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-10 gap-2 text-center px-4">
        <BellOff className="h-8 w-8 text-muted-foreground/40" />
        <p className="text-sm text-muted-foreground">No notifications yet</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-2.5 border-b">
        <span className="text-sm font-semibold">
          Notifications{unreadCount > 0 && <span className="ml-1.5 text-primary">({unreadCount})</span>}
        </span>
        {unreadCount > 0 && (
          <Button
            variant="ghost"
            size="sm"
            className="h-7 gap-1.5 text-xs"
            onClick={onMarkAllRead}
            disabled={isMarkingAll}
          >
            <CheckCheck className="h-3.5 w-3.5" />
            Mark all read
          </Button>
        )}
      </div>

      {/* List */}
      <div className="overflow-y-auto max-h-80">
        <div className="divide-y">
          {notifications.map((n) => (
            <NotificationItem key={n.id} notification={n} onMarkRead={onMarkRead} />
          ))}
        </div>
      </div>
    </div>
  );
}
