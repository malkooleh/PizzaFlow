import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Bell, Settings } from "lucide-react";
import { useAuth } from "react-oidc-context";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { NotificationInbox } from "./NotificationInbox";
import { useInbox, useMarkAsRead, useMarkAllAsRead, useUnreadCount } from "@/hooks/use-notifications";

export function NotificationBell() {
  const auth = useAuth();
  const userId = auth.user?.profile.sub;
  const [open, setOpen] = useState(false);

  const { data: unreadCount = 0 } = useUnreadCount(userId);
  const { data: notifications = [] } = useInbox(userId);

  const markAsRead = useMarkAsRead(userId);
  const markAllAsRead = useMarkAllAsRead(userId);

  const handleMarkRead = (id: string) => markAsRead.mutate(id);
  const handleMarkAllRead = () => markAllAsRead.mutate();

  return (
    <DropdownMenu open={open} onOpenChange={setOpen}>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" aria-label="Notifications" className="relative">
          <Bell className="h-5 w-5" />
          {unreadCount > 0 && (
            <span className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-destructive-foreground">
              {unreadCount > 9 ? "9+" : unreadCount}
            </span>
          )}
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent
        align="end"
        className="w-80 p-0"
        onCloseAutoFocus={(e) => e.preventDefault()}
      >
        <NotificationInbox
          notifications={notifications}
          onMarkRead={handleMarkRead}
          onMarkAllRead={handleMarkAllRead}
          isMarkingAll={markAllAsRead.isPending}
        />

        {/* Footer link to preferences */}
        <div className="border-t px-4 py-2.5">
          <Link
            to="/notifications"
            onClick={() => setOpen(false)}
            className="flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors"
          >
            <Settings className="h-3.5 w-3.5" />
            Notification preferences
          </Link>
        </div>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
