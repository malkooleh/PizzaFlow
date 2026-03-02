import { createFileRoute } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { BookingList } from "@/features/bookings/components/BookingList";

export const Route = createFileRoute("/bookings/")({
  component: BookingsRoute,
});

function BookingsRoute() {
  const auth = useAuth();

  return (
    <ProtectedRoute>
      <div className="container mx-auto max-w-2xl px-4 py-8">
        {/* The booking service uses UUID customer IDs (Keycloak sub),
            NOT the numeric Long IDs used by order/payment services. */}
        <BookingList customerId={auth.user?.profile.sub} />
      </div>
    </ProtectedRoute>
  );
}

