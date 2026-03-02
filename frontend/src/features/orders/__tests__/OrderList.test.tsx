import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { http, HttpResponse } from "msw";
import { server } from "@/test/handlers";
import { renderWithQuery } from "@/test/render-helpers";
import { OrderList } from "../components/OrderList";
import { OrderStatus, OrderType } from "@/types/enums";
import type { OrderResponse } from "@/types/models";

vi.mock("@tanstack/react-router", () => ({
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  Link: ({ to, params, children, className }: any) => (
    <a href={`${to}/${params?.orderId ?? ""}`} className={className}>{children}</a>
  ),
}));

const BASE = "/api/v1";

function makeOrder(id: number, status: OrderStatus, orderType = OrderType.DELIVERY): OrderResponse {
  return {
    id,
    orderNumber: `PF-0${id}`,
    customerId: 1,
    restaurantId: 1,
    orderType,
    status,
    deliveryAddress: "1 Test St",
    items: [{ id: 1, menuItemId: "m1", menuItemName: "Margherita", quantity: 1, unitPrice: 12.99 }],
    subtotal: 12.99,
    tax: 1.30,
    deliveryFee: 2.99,
    totalAmount: 17.28,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
}

const MOCK_ORDERS: OrderResponse[] = [
  makeOrder(1, OrderStatus.PREPARING),
  makeOrder(2, OrderStatus.COMPLETED),
  makeOrder(3, OrderStatus.CANCELLED),
];

function stubOrders(orders: OrderResponse[]) {
  server.use(
    http.get(`${BASE}/orders/customer/:customerId`, () =>
      HttpResponse.json({ success: true, data: orders, message: "OK", timestamp: new Date().toISOString() }),
    ),
  );
}

describe("OrderList", () => {
  it("shows a skeleton while loading", () => {
    stubOrders([]);
    const { container } = renderWithQuery(<OrderList customerId={1} />);
    // Skeleton elements have animate-pulse class from the Skeleton component
    const skeletons = container.querySelectorAll('.animate-pulse');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it("renders order cards after data loads", async () => {
    stubOrders(MOCK_ORDERS);
    renderWithQuery(<OrderList customerId={1} />);

    await waitFor(() => {
      expect(screen.getByText("PF-01")).toBeInTheDocument();
    });
    expect(screen.getByText("PF-02")).toBeInTheDocument();
    expect(screen.getByText("PF-03")).toBeInTheDocument();
  });

  it("shows empty state when there are no orders", async () => {
    stubOrders([]);
    renderWithQuery(<OrderList customerId={1} />);

    await waitFor(() => {
      expect(screen.getByText("No orders yet")).toBeInTheDocument();
    });
  });

  it("filters to active orders on the Active tab", async () => {
    stubOrders(MOCK_ORDERS);
    const user = userEvent.setup();
    renderWithQuery(<OrderList customerId={1} />);

    await waitFor(() => screen.getByText("PF-01")); // wait for data

    await user.click(screen.getByRole("tab", { name: /active/i }));

    expect(screen.getByText("PF-01")).toBeInTheDocument(); // PREPARING is active
    expect(screen.queryByText("PF-02")).not.toBeInTheDocument(); // COMPLETED is not
    expect(screen.queryByText("PF-03")).not.toBeInTheDocument(); // CANCELLED is not
  });

  it("filters to completed orders on the Completed tab", async () => {
    stubOrders(MOCK_ORDERS);
    const user = userEvent.setup();
    renderWithQuery(<OrderList customerId={1} />);

    await waitFor(() => screen.getByText("PF-01"));
    await user.click(screen.getByRole("tab", { name: /completed/i }));

    expect(screen.queryByText("PF-01")).not.toBeInTheDocument();
    expect(screen.getByText("PF-02")).toBeInTheDocument();
    expect(screen.queryByText("PF-03")).not.toBeInTheDocument();
  });

  it("filters to cancelled orders on the Cancelled tab", async () => {
    stubOrders(MOCK_ORDERS);
    const user = userEvent.setup();
    renderWithQuery(<OrderList customerId={1} />);

    await waitFor(() => screen.getByText("PF-01"));
    await user.click(screen.getByRole("tab", { name: /cancelled/i }));

    expect(screen.queryByText("PF-01")).not.toBeInTheDocument();
    expect(screen.queryByText("PF-02")).not.toBeInTheDocument();
    expect(screen.getByText("PF-03")).toBeInTheDocument();
  });

  it("shows an active orders badge count on the Active tab", async () => {
    stubOrders(MOCK_ORDERS);
    renderWithQuery(<OrderList customerId={1} />);

    await waitFor(() => screen.getByText("PF-01"));
    // One active order (PREPARING) → badge should show "1"
    const activeBadge = screen.getByText("1");
    expect(activeBadge).toBeInTheDocument();
  });

  it("shows error state when API fails", async () => {
    server.use(
      http.get(`${BASE}/orders/customer/:customerId`, () =>
        HttpResponse.json({ success: false, data: null, error: "Server error", message: null, timestamp: new Date().toISOString() }, { status: 500 }),
      ),
    );
    renderWithQuery(<OrderList customerId={1} />);

    await waitFor(() => {
      expect(screen.getByText("Failed to load orders.")).toBeInTheDocument();
    });
  });
});
