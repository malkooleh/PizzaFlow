import { Link } from "@tanstack/react-router";
import { Menu, ShoppingCart, Bell } from "lucide-react";
import { useAuth } from "react-oidc-context";
import { Button } from "@/components/ui/button";
import { UserMenu } from "@/components/auth/UserMenu";
import { LoginButton } from "@/components/auth/LoginButton";
import { useUiStore } from "@/stores/ui.store";
import { useCartStore } from "@/stores/cart.store";

export function Header() {
  const auth = useAuth();
  const { toggleSidebar } = useUiStore();
  const totalItems = useCartStore((s) => s.totalItems());

  return (
    <header className="sticky top-0 z-40 flex h-14 items-center gap-4 border-b bg-background px-4 md:px-6">
      {/* Sidebar toggle (desktop) */}
      <Button
        variant="ghost"
        size="icon"
        className="hidden md:flex"
        onClick={toggleSidebar}
        aria-label="Toggle sidebar"
      >
        <Menu className="h-5 w-5" />
      </Button>

      {/* Logo */}
      <Link to="/" className="flex items-center gap-2 font-bold text-primary">
        <span className="text-xl">🍕</span>
        <span className="hidden sm:inline">PizzaFlow</span>
      </Link>

      {/* Spacer */}
      <div className="flex-1" />

      {/* Cart icon (only for customers / guest) */}
      {(!auth.isAuthenticated ||
        auth.user?.profile?.["realm_access"] === undefined ||
        (() => {
          const roles = (
            auth.user?.profile?.["realm_access"] as { roles?: string[] } | undefined
          )?.roles;
          return !roles || roles.includes("CUSTOMER");
        })()) && (
        <Link to="/cart" className="relative">
          <Button variant="ghost" size="icon" aria-label={`Cart — ${totalItems} items`}>
            <ShoppingCart className="h-5 w-5" />
            {totalItems > 0 && (
              <span className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-primary text-[10px] font-bold text-primary-foreground">
                {totalItems > 99 ? "99+" : totalItems}
              </span>
            )}
          </Button>
        </Link>
      )}

      {/* Notification bell (authenticated only) */}
      {auth.isAuthenticated && (
        <Button variant="ghost" size="icon" aria-label="Notifications">
          <Bell className="h-5 w-5" />
        </Button>
      )}

      {/* User menu or login button */}
      {auth.isAuthenticated ? <UserMenu /> : <LoginButton />}
    </header>
  );
}
