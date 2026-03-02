import { useState } from "react";
import { Loader2, CheckCircle2, Truck, Package, MapPin } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import {
  useCompleteDelivery,
  useMarkArrived,
  useMarkInTransit,
  useMarkPickedUp,
} from "@/hooks/use-deliveries";
import { DeliveryStatus } from "@/types/enums";
import type { DeliveryResponse } from "@/types/models";

interface DeliveryStatusFlowProps {
  delivery: DeliveryResponse;
}

export function DeliveryStatusFlow({ delivery }: DeliveryStatusFlowProps) {
  const [notes, setNotes] = useState("");
  const [completeOpen, setCompleteOpen] = useState(false);

  const markPickedUp = useMarkPickedUp();
  const markInTransit = useMarkInTransit();
  const markArrived = useMarkArrived();
  const completeDelivery = useCompleteDelivery();

  const isPending =
    markPickedUp.isPending ||
    markInTransit.isPending ||
    markArrived.isPending ||
    completeDelivery.isPending;

  function handleComplete() {
    completeDelivery.mutate(
      { id: delivery.id, notes: notes.trim() || undefined },
      {
        onSuccess: () => {
          setCompleteOpen(false);
          setNotes("");
        },
      },
    );
  }

  const { status } = delivery;

  return (
    <div className="space-y-2">
      {status === DeliveryStatus.ASSIGNED && (
        <Button
          className="w-full"
          disabled={isPending}
          onClick={() => markPickedUp.mutate(delivery.id)}
        >
          {markPickedUp.isPending ? (
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
          ) : (
            <Package className="mr-2 h-4 w-4" />
          )}
          Mark as Picked Up
        </Button>
      )}

      {status === DeliveryStatus.PICKED_UP && (
        <Button
          className="w-full"
          disabled={isPending}
          onClick={() => markInTransit.mutate(delivery.id)}
        >
          {markInTransit.isPending ? (
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
          ) : (
            <Truck className="mr-2 h-4 w-4" />
          )}
          Start Delivery
        </Button>
      )}

      {status === DeliveryStatus.IN_TRANSIT && (
        <Button
          className="w-full"
          disabled={isPending}
          onClick={() => markArrived.mutate(delivery.id)}
        >
          {markArrived.isPending ? (
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
          ) : (
            <MapPin className="mr-2 h-4 w-4" />
          )}
          Arrived at Destination
        </Button>
      )}

      {status === DeliveryStatus.ARRIVED && (
        <Dialog open={completeOpen} onOpenChange={setCompleteOpen}>
          <DialogTrigger asChild>
            <Button className="w-full" disabled={isPending}>
              <CheckCircle2 className="mr-2 h-4 w-4" />
              Complete Delivery
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Complete Delivery</DialogTitle>
            </DialogHeader>
            <div className="space-y-3 pt-2">
              <div>
                <Label htmlFor="notes">Notes (optional)</Label>
                <Textarea
                  id="notes"
                  placeholder="Any notes for this delivery…"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className="mt-1"
                  rows={3}
                />
              </div>
              <Button
                className="w-full"
                disabled={completeDelivery.isPending}
                onClick={handleComplete}
              >
                {completeDelivery.isPending ? (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                ) : (
                  <CheckCircle2 className="mr-2 h-4 w-4" />
                )}
                Confirm Completion
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}

      {(status === DeliveryStatus.DELIVERED ||
        status === DeliveryStatus.FAILED ||
        status === DeliveryStatus.CANCELLED) && (
        <p className="text-center text-sm text-muted-foreground">
          This delivery is{" "}
          {status === DeliveryStatus.DELIVERED ? "complete" : "closed"}.
        </p>
      )}
    </div>
  );
}
