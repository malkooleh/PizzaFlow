import { render, screen, waitFor } from "@testing-library/react";
import { vi } from "vitest";
import { http, HttpResponse } from "msw";
import { server } from "@/test/handlers";
import { renderWithQuery } from "@/test/render-helpers";
import { OrderDetail } from "../components/OrderDetail";
import { OrderStatus, OrderType, PaymentStatus, PaymentMethodType } from "@/types/enums";
import type { OrderResponse, PaymentResponse } from "@/types/models";

// ReorderButton uses useNavigate; Dialog components don't need router but let's stub navigate
vi.mock("@tanstack/react-router", () => ({
  useNavigate: () => vi.fn(),
  Link: ({ children }: { children: React.ReactNode }) => <span>{children}</span>,
}));

// react-oidc-context is used by CancelOrderDialog via useCancelOrder → not directly,
// but zustand cart store is used by ReorderButton. Calling render won't trigger it
// since we're testing the detail display, not reorder interaction.

const BASE = "/api/v1";

const MOCK_ORDER: OrderResponse = {
  id: 6001,
  orderNumber: "PF-06001",
  customerId: 1,
  restaurantId: 1,
  orderType: OrderType.DELIVERY,
  status: OrderStatus.PREPARING,
  deliveryAddress: "42 Pizza Lane, New York, 10001",
  items: [
    { id: 1, menuItemId: "m1", menuItemName: "Margherita", quantity: 2, unitPrice: 12.99 },
    { id: 2, menuItemId: "m2", menuItemName: "Tiramisu", quantity: 1, unitPrice: 6.50 },
  ],
  subtotal: 32.48,
  tax: 3.25,
  deliveryFee: 2.99,
  totalAmount: 38.72,
  createdAt: "2026-03-01T10:00:00Z",
  updatedAt: "2026-03-01T10:05:00Z",
};

const MOCK_PAYMENT: PaymentResponse = {
  transactionId: "txn-abc123",
  orderId: 6001,
  customerId: 1,
  amount: 38.72,
  currency: "USD",
  status: PaymentStatus.COMPLETED,
  paymentMethodType: PaymentMethodType.CREDIT_CARD,
  createdAt: "2026-03-01T10:00:05Z",
};

function stubOrderAndPayment(order = MOCK_ORDER, payment: PaymentResponse | null = MOCK_PAYMENT) {
  server.use(
    http.get(`${BASE}/orders/:id`, () =>
      HttpResponse.json({ success: true, data: order, message: "OK", timestamp: new Date().toISOString() }),
    ),
  );
  if (payment !== null) {
    server.use(
      http.get(`${BASE}/payments/order/:orderId`, () =>
        HttpResponse.json({ success: true, data: payment, message: "OK", timestamp: new Date().toISOString() }),
      ),
    );
  }
}

describe("OrderDetail", () => {
  it("renders a skeleton while loading", () => {
    stubOrderAndPayment();
    const { container } = renderWithQuery(<OrderDetail orderId={6001} />);
    const skeletons = container.querySelectorAll(".animate-pulse");
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it("displays the order number and status after loading", async () => {
    stubOrderAndPayment();
    renderWithQuery(<OrderDetail orderId={6001} />);

    await waitFor(() => expect(screen.getByText("PF-06001")).toBeInTheDocument());
    expect(screen.getByText("Preparing")).toBeInTheDocument();
  });

  it("lists all items with quantities and prices", async () => {
    stubOrderAndPayment();
    renderWithQuery(<OrderDetail orderId={6001} />);

    await waitFor(() => screen.getByText("PF-06001"));
    expect(screen.getByText("Margherita")).toBeInTheDocument();
    expect(screen.getByText("Tiramisu")).toBeInTheDocument();
    // Quantity badges
    expect(screen.getByText("2")).toBeInTheDocument();
    expect(screen.getByText("1")).toBeInTheDocument();
  });

  it("shows the delivery address", async () => {
    stubOrderAndPayment();
    renderWithQuery(<OrderDetail orderId={6001} />);

    await waitFor(() => screen.getByText("PF-06001"));
    expect(screen.getByText("42 Pizza Lane, New York, 10001")).toBeInTheDocument();
  });

  it("shows the total amount", async () => {
    stubOrderAndPayment();
    renderWithQuery(<OrderDetail orderId={6001} />);

    await waitFor(() => screen.getByText("PF-06001"));
    expect(screen.getByText(/\$38\.72/)).toBeInTheDocument();
  });

  it("shows payment details when available", async () => {
    stubOrderAndPayment();
    renderWithQuery(<OrderDetail orderId={6001} />);

    await waitFor(() => screen.getByText("PF-06001"));
    expect(screen.getByText("Credit Card")).toBeInTheDocument();
    expect(screen.getByText("txn-abc123")).toBeInTheDocument();
  });

  it("shows the cancel button for cancellable orders (PENDING/CONFIRMED)", async () => {
    stubOrderAndPayment({ ...MOCK_ORDER, status: OrderStatus.PENDING });
    renderWithQuery(<OrderDetail orderId={6001} />);

    await waitFor(() => screen.getByText("PF-06001"));
    expect(screen.getByRole("button", { name: /cancel order/i })).toBeInTheDocument();
  });

  it("does not show cancel button for terminal statuses", async () => {
    stubOrderAndPayment({ ...MOCK_ORDER, status: OrderStatus.COMPLETED });
    renderWithQuery(<OrderDetail orderId={6001} />);

    await waitFor(() => screen.getByText("PF-06001"));
    expect(screen.queryByRole("button", { name: /cancel order/i })).not.toBeInTheDocument();
  });

  it("shows reorder button for completed/delivered/cancelled orders", async () => {
    stubOrderAndPayment({ ...MOCK_ORDER, status: OrderStatus.COMPLETED });
    renderWithQuery(<OrderDetail orderId={6001} />);

    await waitFor(() => screen.getByText("PF-06001"));
    expect(screen.getByRole("button", { name: /reorder/i })).toBeInTheDocument();
  });

  it("renders an error state when the API fails", async () => {
    server.use(
      http.get(`${BASE}/orders/:id`, () =>
        HttpResponse.json(
          { success: false, data: null, error: "Not found", message: null, timestamp: new Date().toISOString() },
          { status: 404 },
        ),
      ),
    );
    renderWithQuery(<OrderDetail orderId={9999} />);

    await waitFor(() => {
      expect(screen.getByText("Failed to load order details.")).toBeInTheDocument();
    });
  });
});
