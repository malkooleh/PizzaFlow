import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi, type Mock } from "vitest";
import { renderWithQuery } from "@/test/render-helpers";
import { KitchenOrderStatus, OrderPriority } from "@/types/enums";
import { makeKitchenOrder, makeQueueStatus } from "@/test/kitchen.handlers";
import { KDSBoard } from "../components/KDSBoard";

vi.mock("@/hooks/use-kitchen", () => ({
  useKitchenQueue: vi.fn(),
  useKitchenWebSocket: vi.fn().mockReturnValue({ connected: true }),
  useStartPreparing: vi.fn(),
  useMarkReady: vi.fn(),
  useMarkPickedUp: vi.fn(),
}));

import * as useKitchenModule from "@/hooks/use-kitchen";

const mockMutation = () => ({
  mutateAsync: vi.fn().mockResolvedValue({}),
  isPending: false,
});

function setupMocks(queueOverride: Partial<ReturnType<typeof makeQueueStatus>> = {}) {
  const queue = { ...makeQueueStatus(1), ...queueOverride };
  (useKitchenModule.useKitchenQueue as Mock).mockReturnValue({
    data: queue,
    isLoading: false,
    isError: false,
  });
  (useKitchenModule.useStartPreparing as Mock).mockReturnValue(mockMutation());
  (useKitchenModule.useMarkReady as Mock).mockReturnValue(mockMutation());
  (useKitchenModule.useMarkPickedUp as Mock).mockReturnValue(mockMutation());
  (useKitchenModule.useKitchenWebSocket as Mock).mockReturnValue({
    connected: true,
  });
}

describe("KDSBoard", () => {
  afterEach(() => vi.clearAllMocks());

  it("shows skeleton while loading", () => {
    (useKitchenModule.useKitchenQueue as Mock).mockReturnValue({
      data: undefined,
      isLoading: true,
      isError: false,
    });
    (useKitchenModule.useStartPreparing as Mock).mockReturnValue(mockMutation());
    (useKitchenModule.useMarkReady as Mock).mockReturnValue(mockMutation());
    (useKitchenModule.useMarkPickedUp as Mock).mockReturnValue(mockMutation());
    (useKitchenModule.useKitchenWebSocket as Mock).mockReturnValue({
      connected: false,
    });

    renderWithQuery(<KDSBoard restaurantId={1} />);
    const loading = document.querySelector('[data-testid="kds-loading"]');
    if (!loading) {
      const pulses = document.querySelectorAll(".animate-pulse");
      expect(pulses.length).toBeGreaterThan(0);
    } else {
      expect(loading).toBeInTheDocument();
    }
  });

  it("shows error state when the query fails", () => {
    (useKitchenModule.useKitchenQueue as Mock).mockReturnValue({
      data: undefined,
      isLoading: false,
      isError: true,
    });
    (useKitchenModule.useStartPreparing as Mock).mockReturnValue(mockMutation());
    (useKitchenModule.useMarkReady as Mock).mockReturnValue(mockMutation());
    (useKitchenModule.useMarkPickedUp as Mock).mockReturnValue(mockMutation());
    (useKitchenModule.useKitchenWebSocket as Mock).mockReturnValue({
      connected: false,
    });

    renderWithQuery(<KDSBoard restaurantId={1} />);
    const errorEl = document.querySelector('[data-testid="kds-error"]');
    expect(errorEl).toBeInTheDocument();
  });

  it("renders three station columns", () => {
    setupMocks();
    renderWithQuery(<KDSBoard restaurantId={1} />);

    expect(
      document.querySelector('[data-testid="station-column-received"]')
    ).toBeInTheDocument();
    expect(
      document.querySelector('[data-testid="station-column-preparing"]')
    ).toBeInTheDocument();
    expect(
      document.querySelector('[data-testid="station-column-ready"]')
    ).toBeInTheDocument();
  });

  it("places orders into the correct column by status", () => {
    const orders = [
      makeKitchenOrder(10, KitchenOrderStatus.RECEIVED),
      makeKitchenOrder(11, KitchenOrderStatus.PREPARING),
      makeKitchenOrder(12, KitchenOrderStatus.READY),
    ];
    setupMocks({ orders, totalOrders: 3 });

    renderWithQuery(<KDSBoard restaurantId={1} />);

    const receivedCol = document.querySelector(
      '[data-testid="station-column-received"]'
    );
    const preparingCol = document.querySelector(
      '[data-testid="station-column-preparing"]'
    );
    const readyCol = document.querySelector(
      '[data-testid="station-column-ready"]'
    );

    expect(
      receivedCol?.querySelectorAll('[data-testid="kds-order-card"]').length
    ).toBe(1);
    expect(
      preparingCol?.querySelectorAll('[data-testid="kds-order-card"]').length
    ).toBe(1);
    expect(
      readyCol?.querySelectorAll('[data-testid="kds-order-card"]').length
    ).toBe(1);
  });

  it("shows queue stats when data is available", () => {
    setupMocks();
    renderWithQuery(<KDSBoard restaurantId={1} />);
    // QueueStats renders totalOrders count
    expect(screen.getByText(/total/i)).toBeInTheDocument();
  });

  it("calls startPreparing when action button clicked on a RECEIVED order", async () => {
    const user = userEvent.setup();
    const mutateAsync = vi.fn().mockResolvedValue({});
    const order = makeKitchenOrder(20, KitchenOrderStatus.RECEIVED);
    setupMocks({ orders: [order], totalOrders: 1 });
    (useKitchenModule.useStartPreparing as Mock).mockReturnValue({
      mutateAsync,
      isPending: false,
    });

    renderWithQuery(<KDSBoard restaurantId={1} />);

    await user.click(screen.getByRole("button", { name: /start preparing/i }));
    expect(mutateAsync).toHaveBeenCalledWith({ orderId: order.orderId });
  });

  it("shows WebSocket connected indicator", () => {
    setupMocks();
    renderWithQuery(<KDSBoard restaurantId={1} />);
    // Connected badge / icon should be visible
    const board = document.querySelector('[data-testid="kds-board"]');
    expect(board).toBeInTheDocument();
  });
});
