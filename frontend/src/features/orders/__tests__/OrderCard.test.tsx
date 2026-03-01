import { render, screen } from "@testing-library/react";
import { vi } from "vitest";
import { OrderCard } from "../components/OrderCard";
import { OrderStatus, OrderType } from "@/types/enums";
import type { OrderResponse } from "@/types/models";

// TanStack Router Link needs a router context; replace with a plain anchor for unit tests
vi.mock("@tanstack/react-router", () => ({
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  Link: ({ to, params, children, className }: any) => (
    <a href={`${to}/${params?.orderId ?? ""}`} className={className}>
      {children}
    </a>
  ),
}));

const baseOrder: OrderResponse = {
  id: 5001,
  orderNumber: "PF-05001",
  customerId: 1,
  restaurantId: 1,
  orderType: OrderType.DELIVERY,
  status: OrderStatus.PREPARING,
  deliveryAddress: "1 Test St",
  items: [
    { id: 1, menuItemId: "m1", menuItemName: "Margherita", quantity: 2, unitPrice: 12.99 },
    { id: 2, menuItemId: "m2", menuItemName: "Pepperoni", quantity: 1, unitPrice: 14.99 },
  ],
  subtotal: 40.97,
  tax: 4.10,
  deliveryFee: 2.99,
  totalAmount: 48.06,
  createdAt: new Date("2026-01-01T10:00:00Z").toISOString(),
  updatedAt: new Date("2026-01-01T10:05:00Z").toISOString(),
};

describe("OrderCard", () => {
  it("displays the order number", () => {
    render(<OrderCard order={baseOrder} />);
    expect(screen.getByText("PF-05001")).toBeInTheDocument();
  });

  it("shows a status badge with the order status", () => {
    render(<OrderCard order={baseOrder} />);
    expect(screen.getByText("Preparing")).toBeInTheDocument();
  });

  it("summarises multiple items as 'first item + N more'", () => {
    render(<OrderCard order={baseOrder} />);
    expect(screen.getByText("Margherita + 1 more")).toBeInTheDocument();
  });

  it("shows the single item name when there is only one item", () => {
    const singleItemOrder = { ...baseOrder, items: [baseOrder.items[0]] };
    render(<OrderCard order={singleItemOrder} />);
    expect(screen.getByText("Margherita")).toBeInTheDocument();
  });

  it("displays the formatted total amount", () => {
    render(<OrderCard order={baseOrder} />);
    // $48.06 formatted as currency
    expect(screen.getByText(/\$48\.06/)).toBeInTheDocument();
  });

  it("shows an active pulse indicator for in-progress orders", () => {
    const { container } = render(<OrderCard order={baseOrder} />);
    // The pulse dot has animate-pulse class
    const pulseDot = container.querySelector(".animate-pulse");
    expect(pulseDot).toBeInTheDocument();
  });

  it("does not show a pulse indicator for completed orders", () => {
    const completedOrder = { ...baseOrder, status: OrderStatus.COMPLETED };
    const { container } = render(<OrderCard order={completedOrder} />);
    const pulseDot = container.querySelector(".animate-pulse");
    expect(pulseDot).not.toBeInTheDocument();
  });

  it("links to the order detail page", () => {
    render(<OrderCard order={baseOrder} />);
    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("href", expect.stringContaining("5001"));
  });

  it("shows all item count", () => {
    render(<OrderCard order={baseOrder} />);
    expect(screen.getByText(/2 items/)).toBeInTheDocument();
  });
});
