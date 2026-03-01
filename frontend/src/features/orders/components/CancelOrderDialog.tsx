import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { useCancelOrder } from "@/hooks/use-orders";
import { toast } from "sonner";
import { XCircle } from "lucide-react";

interface CancelOrderDialogProps {
  orderId: number;
  orderNumber: string;
  onCancelled?: () => void;
}

export function CancelOrderDialog({
  orderId,
  orderNumber,
  onCancelled,
}: CancelOrderDialogProps) {
  const [open, setOpen] = useState(false);
  const [reason, setReason] = useState("");
  const cancelOrder = useCancelOrder();

  async function handleConfirm() {
    try {
      await cancelOrder.mutateAsync({ id: orderId, reason: reason.trim() || undefined });
      toast.success(`Order ${orderNumber} has been cancelled.`);
      setOpen(false);
      setReason("");
      onCancelled?.();
    } catch {
      toast.error("Failed to cancel the order. Please try again.");
    }
  }

  function handleOpenChange(nextOpen: boolean) {
    if (!nextOpen) setReason("");
    setOpen(nextOpen);
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button
          variant="outline"
          size="sm"
          className="text-destructive border-destructive/30 hover:bg-destructive/5"
        >
          <XCircle className="mr-2 h-4 w-4" />
          Cancel Order
        </Button>
      </DialogTrigger>

      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Cancel order {orderNumber}?</DialogTitle>
          <DialogDescription>
            This action cannot be undone. Your order will be cancelled and any payment
            may be refunded according to our policy.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-2 py-2">
          <Label htmlFor="cancel-reason" className="text-sm font-medium">
            Reason <span className="text-muted-foreground font-normal">(optional)</span>
          </Label>
          <Textarea
            id="cancel-reason"
            placeholder="Tell us why you're cancelling…"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={3}
            maxLength={300}
          />
        </div>

        <DialogFooter className="gap-2">
          <Button
            variant="outline"
            disabled={cancelOrder.isPending}
            onClick={() => handleOpenChange(false)}
          >
            Keep Order
          </Button>
          <Button
            variant="destructive"
            disabled={cancelOrder.isPending}
            onClick={() => void handleConfirm()}
          >
            {cancelOrder.isPending ? "Cancelling…" : "Yes, Cancel"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
