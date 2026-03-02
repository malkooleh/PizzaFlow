import { setupServer } from "msw/node";
import { catalogHandlers } from "./catalog.handlers";
import { menuHandlers } from "./menu.handlers";
import { ordersHandlers } from "./orders.handlers";
import { bookingsHandlers } from "./bookings.handlers";
import { kitchenHandlers } from "./kitchen.handlers";

/**
 * MSW server instance — shared between Vitest (unit/integration tests)
 * and development mode (mock API when backend is down).
 * menuHandlers are registered first so their more-specific paths
 * take precedence over the wildcard-glob catalogHandlers.
 */
export const server = setupServer(
  ...menuHandlers,
  ...ordersHandlers,
  ...bookingsHandlers,
  ...kitchenHandlers,
  ...catalogHandlers,
);
