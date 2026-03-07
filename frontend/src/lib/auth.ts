import { UserManager } from "oidc-client-ts";
import type { User } from "oidc-client-ts";
import type { AuthContextProps } from "react-oidc-context";
import type { UserRole } from "@/types/enums";

/**
 * OIDC configuration for react-oidc-context.
 * Reads Keycloak connection details from Vite env variables.
 * PKCE with S256 code_challenge_method is enforced on the Keycloak client.
 */
export const oidcConfig = {
  authority: `${import.meta.env.VITE_KEYCLOAK_URL}/realms/${import.meta.env.VITE_KEYCLOAK_REALM}`,
  client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID as string,
  redirect_uri: `${window.location.origin}/callback`,
  post_logout_redirect_uri: window.location.origin,
  scope: "openid profile email roles",
  response_type: "code",
  automaticSilentRenew: true,
  // Tokens are stored in memory only (not localStorage) for XSS safety
  userStore: undefined,
  onSigninCallback: () => {
    // Remove OIDC params from URL after successful login callback
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};

/**
 * Shared UserManager singleton — used by react-oidc-context (AuthProvider) and
 * the API client (client.ts) so both operate on the same token storage.
 */
export const userManager = new UserManager(oidcConfig);

/**
 * Extract realm roles from the Keycloak JWT.
 * Keycloak places realm roles at: token.realm_access.roles
 */
export function getRoles(user: User | null | undefined): UserRole[] {
  if (!user?.profile) return [];
  const profile = user.profile as Record<string, unknown>;
  const realmAccess = profile["realm_access"] as { roles?: string[] } | undefined;
  return (realmAccess?.roles ?? []) as UserRole[];
}

/** Returns true if the authenticated user has at least one of the required roles. */
export function hasRole(auth: AuthContextProps, roles: UserRole[]): boolean {
  if (!auth.isAuthenticated || !auth.user) return false;
  const userRoles = getRoles(auth.user);
  return roles.some((r) => userRoles.includes(r));
}

/** Throws a redirect to /login if the user does not have one of the required roles. */
export function requireRole(auth: AuthContextProps, roles: UserRole[]): void {
  if (!auth.isAuthenticated) {
    throw new Error("NOT_AUTHENTICATED");
  }
  if (!hasRole(auth, roles)) {
    throw new Error("FORBIDDEN");
  }
}

/** Returns the display name from the OIDC profile. */
export function getDisplayName(user: User | null | undefined): string {
  if (!user?.profile) return "User";
  return (
    (user.profile.name as string | undefined) ??
    (user.profile.preferred_username as string | undefined) ??
    "User"
  );
}

/**
 * Returns the numeric database customer ID from the JWT.
 *
 * Keycloak should be configured with a custom token mapper that adds a
 * `customer_id` claim (integer) mapped from the user attribute. Until that
 * mapper is in place, this function falls back to 1 (suitable for dev/MSW).
 *
 * Backend requirement: CreateOrderRequest.customerId and PaymentRequest.customerId
 * both expect a Long.
 */
export function getCustomerDbId(user: User | null | undefined): number {
  if (!user?.profile) return 1;
  const profile = user.profile as Record<string, unknown>;
  const claim = profile["customer_id"];
  if (typeof claim === "number") return claim;
  if (typeof claim === "string") {
    const parsed = parseInt(claim, 10);
    if (!isNaN(parsed)) return parsed;
  }
  // Fallback: derive a stable int from the Keycloak subject UUID
  // (last 8 hex chars → int, capped at 1 000 000 to stay within Long range)
  const sub = user.profile.sub ?? "";
  const lastHex = sub.replace(/-/g, "").slice(-8);
  return (parseInt(lastHex, 16) % 1_000_000) || 1;
}
