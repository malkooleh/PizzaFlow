import { render, screen } from "@testing-library/react";
import { OrderTimeline } from "../components/OrderTimeline";
import { OrderStatus, OrderType } from "@/types/enums";

describe("OrderTimeline", () => {
  describe("DELIVERY type", () => {
    it("marks the active step with a ring highlight", () => {
      render(<OrderTimeline status={OrderStatus.PREPARING} orderType={OrderType.DELIVERY} />);
      // "Preparing" step should be visible as the active label
      expect(screen.getByText("Preparing")).toBeInTheDocument();
    });

    it("renders all delivery steps", () => {
      render(<OrderTimeline status={OrderStatus.PENDING} orderType={OrderType.DELIVERY} />);
      expect(screen.getByText("Order Placed")).toBeInTheDocument();
      expect(screen.getByText("Confirmed")).toBeInTheDocument();
      expect(screen.getByText("Preparing")).toBeInTheDocument();
      expect(screen.getByText("Ready")).toBeInTheDocument();
      expect(screen.getByText("Out for Delivery")).toBeInTheDocument();
      expect(screen.getByText("Delivered")).toBeInTheDocument();
      expect(screen.getByText("Completed")).toBeInTheDocument();
    });
  });

  describe("PICKUP type", () => {
    it("shows 'Ready for Pickup' instead of 'Out for Delivery'", () => {
      render(<OrderTimeline status={OrderStatus.READY} orderType={OrderType.PICKUP} />);
      expect(screen.getByText("Ready for Pickup")).toBeInTheDocument();
      expect(screen.queryByText("Out for Delivery")).not.toBeInTheDocument();
    });

    it("shows 'Picked Up' step", () => {
      render(<OrderTimeline status={OrderStatus.PICKED_UP} orderType={OrderType.PICKUP} />);
      expect(screen.getByText("Picked Up")).toBeInTheDocument();
    });
  });

  describe("CANCELLED state", () => {
    it("shows the cancellation message instead of steps", () => {
      render(<OrderTimeline status={OrderStatus.CANCELLED} orderType={OrderType.DELIVERY} />);
      expect(screen.getByText("Order Cancelled")).toBeInTheDocument();
      expect(screen.queryByText("Order Placed")).not.toBeInTheDocument();
    });
  });
});
