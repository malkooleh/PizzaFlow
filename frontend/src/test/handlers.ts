import { setupServer } from "msw/node";
import { catalogHandlers } from "./catalog.handlers";

/**
 * MSW server instance — shared between Vitest (unit/integration tests)
 * and development mode (mock API when backend is down).
 * Handlers are split by domain and combined here.
 */
export const server = setupServer(...catalogHandlers);
