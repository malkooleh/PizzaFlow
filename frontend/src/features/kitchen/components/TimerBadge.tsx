import { useEffect, useState } from "react";
import { cn } from "@/lib/utils";

interface TimerBadgeProps {
  /** ISO-8601 datetime string — when the order entered the kitchen queue. */
  receivedAt: string;
  className?: string;
}

function getElapsedMs(isoDate: string): number {
  return Date.now() - new Date(isoDate).getTime();
}

function formatElapsed(ms: number): string {
  const totalSeconds = Math.floor(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}m ${String(seconds).padStart(2, "0")}s`;
}

/**
 * Displays elapsed time since an order entered the queue.
 * Updates every second. Color coding:
 *   green  = < 10 min (within target prep time)
 *   yellow = 10–20 min (approaching limit)
 *   red    = > 20 min (overdue — pulses for urgency)
 */
export function TimerBadge({ receivedAt, className }: TimerBadgeProps) {
  const [elapsed, setElapsed] = useState(() => getElapsedMs(receivedAt));

  useEffect(() => {
    const interval = setInterval(() => {
      setElapsed(getElapsedMs(receivedAt));
    }, 1000);
    return () => clearInterval(interval);
  }, [receivedAt]);

  const minutes = elapsed / 60_000;
  const colorClass =
    minutes < 10
      ? "bg-green-100 text-green-700"
      : minutes < 20
        ? "bg-yellow-100 text-yellow-700"
        : "bg-red-100 text-red-700 animate-pulse";

  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold tabular-nums",
        colorClass,
        className
      )}
      aria-label={`Waiting time: ${formatElapsed(elapsed)}`}
    >
      {formatElapsed(elapsed)}
    </span>
  );
}
