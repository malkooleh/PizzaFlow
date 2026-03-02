import { useState } from "react";
import { Loader2, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { useCancelBooking } from "@/hooks/use-bookings";

interface CancelBookingDialogProps {
  bookingId: string;
  bookingNumber: string;
  /** Callback invoked after successful cancellation. */
  onCancelled?: () => void;
}

export function CancelBookingDialog({
  bookingId,
  bookingNumber,
  onCancelled,
}: CancelBookingDialogProps) {
  const [open, setOpen] = useState(false);
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);

  const cancelBooking = useCancelBooking();

  async function handleCancel() {
    setError(null);
    try {
      await cancelBooking.mutateAsync({
        id: bookingId,
        reason: reason.trim() || undefined,
      });
      setOpen(false);
      setReason("");
      onCancelled?.();
    } catch {
      setError("Failed to cancel booking. Please try again.");
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(v) => {
        setOpen(v);
        if (!v) {
          setReason("");
          setError(null);
        }
      }}
    >
      <DialogTrigger asChild>
        <Button variant="outline" size="sm" className="gap-1.5 text-destructive border-destructive/40 hover:bg-destructive/5">
          <X className="h-3.5 w-3.5" />
          Cancel booking
        </Button>
      </DialogTrigger>

      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Cancel booking {bookingNumber}?</DialogTitle>
          <DialogDescription>
            Your table reservation will be released. This action cannot be undone.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-2">
          <Label htmlFor="cancel-reason">
            Reason <span className="text-muted-foreground text-xs">(optional)</span>
          </Label>
          <Textarea
            id="cancel-reason"
            placeholder="e.g. Change of plans"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={3}
            maxLength={500}
          />
          {error && <p className="text-sm text-destructive">{error}</p>}
        </div>

        <DialogFooter className="gap-2">
          <Button
            variant="ghost"
            onClick={() => setOpen(false)}
            disabled={cancelBooking.isPending}
          >
            Keep booking
          </Button>
          <Button
            variant="destructive"
            onClick={handleCancel}
            disabled={cancelBooking.isPending}
          >
            {cancelBooking.isPending ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin mr-1.5" />
                Cancelling…
              </>
            ) : (
              "Yes, cancel"
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
