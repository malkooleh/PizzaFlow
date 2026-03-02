import { useEffect, useRef } from "react";
import { useUpdateCourierLocation } from "@/hooks/use-deliveries";

interface LocationTrackerProps {
  courierId: string;
  /** Reporting interval in milliseconds. Default 10 000. */
  intervalMs?: number;
  onError?: (message: string) => void;
}

/**
 * Headless component — renders nothing.
 * Watches the device GPS position and pushes updates to the backend
 * every `intervalMs` milliseconds.
 */
export function LocationTracker({
  courierId,
  intervalMs = 10_000,
  onError,
}: LocationTrackerProps) {
  const updateLocation = useUpdateCourierLocation();
  const lastSentRef = useRef<number>(0);

  useEffect(() => {
    if (!navigator.geolocation) {
      onError?.("Geolocation not supported");
      return;
    }

    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        const now = Date.now();
        if (now - lastSentRef.current < intervalMs) return;

        lastSentRef.current = now;
        updateLocation.mutate({
          courierId,
          location: {
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude,
          },
        });
      },
      (err) => {
        onError?.(err.message);
      },
      { enableHighAccuracy: true, maximumAge: 5_000 },
    );

    return () => {
      navigator.geolocation.clearWatch(watchId);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [courierId, intervalMs]);

  return null;
}
