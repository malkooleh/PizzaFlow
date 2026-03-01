import "@testing-library/jest-dom";
import { afterEach } from "vitest";
import { cleanup } from "@testing-library/react";
import { server } from "./handlers";

// Clean up after each test to prevent state leakage
afterEach(() => {
  cleanup();
});

// Start the MSW server for all tests
beforeAll(() => server.listen({ onUnhandledRequest: "warn" }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
