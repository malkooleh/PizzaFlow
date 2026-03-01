import { useAuth } from "react-oidc-context";
import { Button } from "@/components/ui/button";
import { LogIn } from "lucide-react";

export function LoginButton() {
  const auth = useAuth();

  return (
    <Button onClick={() => void auth.signinRedirect()} size="sm">
      <LogIn className="h-4 w-4" />
      Sign In
    </Button>
  );
}
