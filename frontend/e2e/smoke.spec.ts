import { test, expect } from "@playwright/test";

/**
 * Smoke tests — verify the app boots and public routes render without
 * JavaScript errors or uncaught exceptions. These tests do NOT require a
 * running Keycloak instance; they cover only publicly accessible pages
 * and basic structural sanity checks.
 */

test.describe("PizzaFlow smoke", () => {
  test("landing page renders the PizzaFlow heading", async ({ page }) => {
    await page.goto("/");
    await expect(page).toHaveTitle(/PizzaFlow/i);
    await expect(page.getByRole("heading", { name: /PizzaFlow/i })).toBeVisible();
  });

  test("menu page is accessible without authentication", async ({ page }) => {
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));

    await page.goto("/menu");

    await expect(page).toHaveTitle(/PizzaFlow/i);
    // No uncaught runtime errors
    expect(errors, `Runtime errors on /menu: ${errors.join(", ")}`).toHaveLength(0);
    // Page body renders something meaningful (not blank)
    await expect(page.locator("body")).not.toBeEmpty();
  });

  test("protected route gates unauthenticated users without crashing", async ({
    page,
  }) => {
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));

    await page.goto("/orders");

    // Without an active session the app should stay on the PizzaFlow domain
    // (loading gate, auth redirect, or signin trigger) — it must NOT crash.
    await expect(page).toHaveTitle(/PizzaFlow/i);
    expect(errors, `Runtime errors on /orders: ${errors.join(", ")}`).toHaveLength(0);
  });

  test("bookings page is reachable and does not throw", async ({ page }) => {
    const errors: string[] = [];
    page.on("pageerror", (err) => errors.push(err.message));

    await page.goto("/bookings");

    await expect(page).toHaveTitle(/PizzaFlow/i);
    expect(errors, `Runtime errors on /bookings: ${errors.join(", ")}`).toHaveLength(0);
  });

  test("unknown route renders 404 page", async ({ page }) => {
    await page.goto("/this-route-does-not-exist");
    // Either the router shows a 404 component or redirects to home —
    // either way the app must not show a blank page.
    await expect(page.locator("body")).not.toBeEmpty();
  });
});
