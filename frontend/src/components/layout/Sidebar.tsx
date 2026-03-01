import { Link, useRouterState } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import {
  UtensilsCrossed,
  ShoppingBag,
  CalendarDays,
  Bell,
  ChefHat,
  Truck,
  LayoutDashboard,
  BookOpen,
  Package,
  BarChart3,
  Activity,
  ShieldCheck,
  History,
  AlertTriangle,
  Search,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { getRoles } from "@/lib/auth";
import { UserRole } from "@/types/enums";
import { useUiStore } from "@/stores/ui.store";
import { Separator } from "@/components/ui/separator";

interface NavItem {
  label: string;
  to: string;
  icon: React.ElementType;
  roles: UserRole[];
}

const NAV_ITEMS: NavItem[] = [
  // Customer
  { label: "Menu", to: "/menu", icon: Search, roles: [UserRole.CUSTOMER] },
  { label: "My Orders", to: "/orders", icon: ShoppingBag, roles: [UserRole.CUSTOMER] },
  { label: "Bookings", to: "/bookings", icon: CalendarDays, roles: [UserRole.CUSTOMER] },
  { label: "Notifications", to: "/notifications", icon: Bell, roles: [UserRole.CUSTOMER] },
  // Kitchen
  {
    label: "Kitchen Board",
    to: "/kitchen",
    icon: ChefHat,
    roles: [UserRole.KITCHEN_STAFF, UserRole.RESTAURANT_MANAGER, UserRole.SYSTEM_ADMIN],
  },
  // Courier
  {
    label: "Deliveries",
    to: "/courier",
    icon: Truck,
    roles: [UserRole.COURIER, UserRole.SYSTEM_ADMIN],
  },
  // Manager
  {
    label: "Dashboard",
    to: "/manager",
    icon: LayoutDashboard,
    roles: [UserRole.RESTAURANT_MANAGER, UserRole.SYSTEM_ADMIN],
  },
  {
    label: "Menu Mgmt",
    to: "/manager/menu",
    icon: BookOpen,
    roles: [UserRole.RESTAURANT_MANAGER, UserRole.SYSTEM_ADMIN],
  },
  {
    label: "Bookings",
    to: "/manager/bookings",
    icon: CalendarDays,
    roles: [UserRole.RESTAURANT_MANAGER, UserRole.SYSTEM_ADMIN],
  },
  {
    label: "Inventory",
    to: "/manager/inventory",
    icon: Package,
    roles: [UserRole.RESTAURANT_MANAGER, UserRole.SYSTEM_ADMIN],
  },
  {
    label: "Analytics",
    to: "/manager/analytics",
    icon: BarChart3,
    roles: [UserRole.RESTAURANT_MANAGER, UserRole.SYSTEM_ADMIN],
  },
  // Admin
  {
    label: "Admin Overview",
    to: "/admin",
    icon: ShieldCheck,
    roles: [UserRole.SYSTEM_ADMIN],
  },
  {
    label: "Services",
    to: "/admin/services",
    icon: Activity,
    roles: [UserRole.SYSTEM_ADMIN],
  },
  {
    label: "Platform Analytics",
    to: "/admin/analytics",
    icon: UtensilsCrossed,
    roles: [UserRole.SYSTEM_ADMIN],
  },
  {
    label: "Audit Feed",
    to: "/admin/audit",
    icon: History,
    roles: [UserRole.SYSTEM_ADMIN],
  },
  {
    label: "Alerts",
    to: "/admin/alerts",
    icon: AlertTriangle,
    roles: [UserRole.SYSTEM_ADMIN],
  },
];

export function Sidebar() {
  const auth = useAuth();
  const { sidebarCollapsed } = useUiStore();
  const routerState = useRouterState();
  const currentPath = routerState.location.pathname;

  const userRoles = getRoles(auth.user);

  const visibleItems = NAV_ITEMS.filter((item) =>
    item.roles.some((r) => userRoles.includes(r))
  );

  return (
    <>
      {/* Desktop sidebar content */}
      <nav className="flex flex-1 flex-col gap-1 overflow-y-auto px-2 py-4">
        {visibleItems.map((item, index) => {
          const isActive =
            currentPath === item.to || currentPath.startsWith(`${item.to}/`);

          // Insert separators between role groups
          const prevItem = visibleItems[index - 1];
          const showSeparator =
            index > 0 &&
            prevItem &&
            !prevItem.roles.some((r) => item.roles.includes(r));

          return (
            <div key={item.to}>
              {showSeparator && <Separator className="my-2" />}
              <Link
                to={item.to}
                className={cn(
                  "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                  "hover:bg-accent hover:text-accent-foreground",
                  isActive
                    ? "bg-primary text-primary-foreground"
                    : "text-muted-foreground"
                )}
                aria-current={isActive ? "page" : undefined}
              >
                <item.icon className="h-4 w-4 shrink-0" aria-hidden="true" />
                {!sidebarCollapsed && <span>{item.label}</span>}
              </Link>
            </div>
          );
        })}
      </nav>

      {/* Mobile bottom tab bar */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 z-40 flex border-t bg-background">
        {visibleItems.slice(0, 5).map((item) => {
          const isActive = currentPath === item.to;
          return (
            <Link
              key={item.to}
              to={item.to}
              className={cn(
                "flex flex-1 flex-col items-center gap-1 py-2 text-xs",
                isActive ? "text-primary" : "text-muted-foreground"
              )}
            >
              <item.icon className="h-5 w-5" aria-hidden="true" />
              <span className="truncate">{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </>
  );
}
