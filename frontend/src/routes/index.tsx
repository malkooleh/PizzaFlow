import { createFileRoute, Link } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import { Button } from "@/components/ui/button";
import { LoginButton } from "@/components/auth/LoginButton";
import { UserRole } from "@/types/enums";
import { getRoles } from "@/lib/auth";

export const Route = createFileRoute("/")({
  component: LandingPage,
});

function LandingPage() {
  const auth = useAuth();
  const roles = getRoles(auth.user);

  // Redirect authenticated users to their home area via link suggestions
  const getHomeLink = () => {
    if (roles.includes(UserRole.SYSTEM_ADMIN)) return "/admin";
    if (roles.includes(UserRole.RESTAURANT_MANAGER)) return "/manager";
    if (roles.includes(UserRole.KITCHEN_STAFF)) return "/kitchen";
    if (roles.includes(UserRole.COURIER)) return "/courier";
    return "/menu";
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-8 p-6 text-center">
      <div className="space-y-4">
        <div className="text-6xl">🍕</div>
        <h1 className="text-4xl font-bold tracking-tight">Welcome to PizzaFlow</h1>
        <p className="text-lg text-muted-foreground max-w-md">
          Cloud-native pizzeria management — orders, kitchen, delivery, and more.
        </p>
      </div>

      <div className="flex gap-4">
        {auth.isAuthenticated ? (
          <Button asChild size="lg">
            <Link to={getHomeLink()}>Go to Dashboard</Link>
          </Button>
        ) : (
          <>
            <LoginButton />
            <Button asChild variant="outline" size="lg">
              <Link to="/menu">Browse Menu</Link>
            </Button>
          </>
        )}
      </div>
    </div>
  );
}
