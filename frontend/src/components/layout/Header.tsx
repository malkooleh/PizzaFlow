import { Link } from "@tanstack/react-router";
import { Menu } from "lucide-react";
import { useAuth } from "react-oidc-context";
import { Button } from "@/components/ui/button";
import { UserMenu } from "@/components/auth/UserMenu";
import { LoginButton } from "@/components/auth/LoginButton";
import { CartSheet } from "@/features/cart/components/CartSheet";
import { NotificationBell } from "@/features/notifications/components/NotificationBell";
import { ThemeToggle } from "@/components/common/ThemeToggle";
import { useUiStore } from "@/stores/ui.store";

export function Header() {
  const auth = useAuth();
  const { toggleSidebar } = useUiStore();

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

      {/* Cart — slide-out sheet for customers / guests */}
      {(!auth.isAuthenticated ||
        (() => {
          const roles = (auth.user?.profile?.["realm_access"] as { roles?: string[] } | undefined)?.roles;
          return !roles || roles.includes("CUSTOMER");
        })()) && <CartSheet />}

      {/* Notification bell (authenticated only) */}
      {auth.isAuthenticated && <NotificationBell />}

      {/* Theme toggle */}
      <ThemeToggle />

      {/* User menu or login button */}
      {auth.isAuthenticated ? <UserMenu /> : <LoginButton />}
    </header>
  );
}
