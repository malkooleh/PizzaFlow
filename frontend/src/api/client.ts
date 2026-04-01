import ky, { type KyResponse } from "ky";
import { toast } from "sonner";
import { userManager } from "@/lib/auth";

/**
 * Configured ky instance — all API calls must go through this client.
 *
 * - prefixUrl: empty string in dev (Vite proxy rewrites /api → gateway),
 *              set to VITE_API_URL in production builds.
 * - Authorization header is injected before each request from the OIDC token.
 * - 401 responses attempt a silent token refresh via the auth module.
 * - 4xx/5xx responses are surfaced as typed errors.
 */

/** Retrieve the current access token from the OIDC user store. */
function getAccessToken(): string | null {
  // oidc-client-ts stores user data in sessionStorage under this key pattern
  const keycloakUrl = import.meta.env.VITE_KEYCLOAK_URL;
  const realm = import.meta.env.VITE_KEYCLOAK_REALM;
  const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID;
  const storageKey = `oidc.user:${keycloakUrl}/realms/${realm}:${clientId}`;
  const raw = sessionStorage.getItem(storageKey);
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw) as { access_token?: string };
    return parsed.access_token ?? null;
  } catch {
    return null;
  }
}

export const apiClient = ky.create({
  prefixUrl: import.meta.env.VITE_API_URL,
  timeout: 15_000,
  retry: {
    limit: 1,
    statusCodes: [408, 500, 502, 503, 504],
    methods: ["get"],
  },
  hooks: {
    beforeRequest: [
      (request) => {
        const token = getAccessToken();
        if (token) {
          request.headers.set("Authorization", `Bearer ${token}`);
        }
      },
    ],
    afterResponse: [
      async (_request, _options, response: KyResponse) => {
        if (response.status === 401) {
          try {
            await userManager.signinSilent();
            // Token refreshed silently in storage — caller can retry the request
          } catch {
            toast.error("Session expired — please sign in again.");
            window.location.href = "/login";
          }
        }
        if (response.status === 403) {
          toast.error("You do not have permission to perform this action.");
        }
        return response;
      },
    ],
    beforeError: [
      async (error) => {
        const { response } = error;
        if (response) {
          type ErrorBody = { message?: string; error?: string };
          try {
            const body: ErrorBody = await response.json();
            error.message = body.message ?? body.error ?? `HTTP ${response.status}`;
          } catch {
            error.message = `HTTP ${response.status}`;
          }
        }
        return error;
      },
    ],
  },
});

/**
 * Alias so all API modules can `import { api } from "./client"`.
 * `apiClient` is the canonical name; `api` is the shorter convenience re-export.
 */
export const api = apiClient;
