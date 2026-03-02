import { http, HttpResponse, delay } from "msw";
import type { BookingResponse, AvailabilityResponse, BookingRestaurant } from "@/types/models";
import { BookingStatus, TableType } from "@/types/enums";

// ── Seed data ─────────────────────────────────────────────────────────────────

const MOCK_RESTAURANTS: BookingRestaurant[] = [
  {
    id: "a1b2c3d4-0001-0001-0001-000000000001",
    name: "Pizza Palace",
    address: "123 Broadway, New York, NY 10001",
    openingTime: "11:00:00",
    closingTime: "23:00:00",
    maxPartySize: 12,
    bookingSlotDurationMinutes: 90,
    isActive: true,
  },
  {
    id: "a1b2c3d4-0002-0002-0002-000000000002",
    name: "La Trattoria",
    address: "456 5th Ave, New York, NY 10018",
    openingTime: "12:00:00",
    closingTime: "22:00:00",
    maxPartySize: 10,
    bookingSlotDurationMinutes: 90,
    isActive: true,
  },
];

/** Generate an ISO datetime string relative to now. */
function relIso(dayOffset: number, hour: number, minute = 0): string {
  const d = new Date();
  d.setDate(d.getDate() + dayOffset);
  d.setHours(hour, minute, 0, 0);
  return d.toISOString().replace(/\.\d{3}Z$/, "");
}

const bookingStore: BookingResponse[] = [
  {
    id: "bb000001-0000-0000-0000-000000000001",
    bookingNumber: "BK-20260301-001",
    customerId: "customer-uuid-sub",
    customerName: "Jane Customer",
    customerPhone: "+1 555 111 0001",
    customerEmail: "customer@example.com",
    restaurantId: MOCK_RESTAURANTS[0].id,
    restaurantName: "Pizza Palace",
    tableId: "tt000001-0000-0000-0000-000000000001",
    tableName: "Window Table 4",
    tableType: TableType.INDOOR,
    reservationTime: relIso(2, 19, 0),
    endTime: relIso(2, 20, 30),
    partySize: 2,
    status: BookingStatus.CONFIRMED,
    specialRequests: "Window seat please",
    preOrderId: null,
    createdAt: relIso(-1, 10, 0),
    updatedAt: relIso(-1, 10, 5),
  },
  {
    id: "bb000002-0000-0000-0000-000000000002",
    bookingNumber: "BK-20260220-001",
    customerId: "customer-uuid-sub",
    customerName: "Jane Customer",
    customerPhone: "+1 555 111 0001",
    customerEmail: "customer@example.com",
    restaurantId: MOCK_RESTAURANTS[1].id,
    restaurantName: "La Trattoria",
    tableId: "tt000002-0000-0000-0000-000000000002",
    tableName: "Table 8",
    tableType: TableType.OUTDOOR,
    reservationTime: relIso(-9, 18, 30),
    endTime: relIso(-9, 20, 0),
    partySize: 3,
    status: BookingStatus.COMPLETED,
    specialRequests: null,
    preOrderId: null,
    createdAt: relIso(-12, 14, 0),
    updatedAt: relIso(-9, 20, 5),
  },
  {
    id: "bb000003-0000-0000-0000-000000000003",
    bookingNumber: "BK-20260210-001",
    customerId: "customer-uuid-sub",
    customerName: "Jane Customer",
    customerPhone: "+1 555 111 0001",
    customerEmail: "customer@example.com",
    restaurantId: MOCK_RESTAURANTS[0].id,
    restaurantName: "Pizza Palace",
    tableId: null,
    tableName: null,
    tableType: null,
    reservationTime: relIso(-19, 20, 0),
    endTime: relIso(-19, 21, 30),
    partySize: 4,
    status: BookingStatus.CANCELLED,
    specialRequests: null,
    preOrderId: null,
    createdAt: relIso(-22, 9, 0),
    updatedAt: relIso(-21, 11, 0),
  },
];

let nextBookingSeq = 4;

function makeBookingNumber(): string {
  const now = new Date();
  const yyyymmdd = now.toISOString().slice(0, 10).replaceAll("-", "");
  return `BK-${yyyymmdd}-${String(nextBookingSeq++).padStart(3, "0")}`;
}

/** Build availability slots for a date, dynamically so tests always get future data. */
function buildAvailability(restaurantId: string, date: string, partySize: number): AvailabilityResponse {
  const restaurant = MOCK_RESTAURANTS.find((r) => r.id === restaurantId);
  if (!restaurant) {
    return {
      restaurantId,
      restaurantName: "Unknown",
      date,
      requestedPartySize: partySize,
      availableSlots: [],
      totalCapacity: 0,
      fullyBooked: true,
    };
  }

  const slots = [18, 18.5, 19, 19.5, 20, 20.5].map((h) => {
    const hInt = Math.floor(h);
    const mInt = (h % 1) * 60;
    const start = `${date}T${String(hInt).padStart(2, "0")}:${String(mInt).padStart(2, "0")}:00`;
    const endH = hInt + 1;
    const end = `${date}T${String(endH).padStart(2, "0")}:${String(mInt).padStart(2, "0")}:00`;
    return {
      startTime: start,
      endTime: end,
      availableCapacity: restaurant.maxPartySize - partySize,
      availableTables: [
        {
          tableId: "tt000001-0000-0000-0000-000000000001",
          tableName: "Table A",
          capacity: 4,
          tableType: TableType.INDOOR,
        },
        {
          tableId: "tt000002-0000-0000-0000-000000000002",
          tableName: "Patio 1",
          capacity: 6,
          tableType: TableType.OUTDOOR,
        },
      ],
    };
  });

  return {
    restaurantId: restaurant.id,
    restaurantName: restaurant.name,
    date,
    requestedPartySize: partySize,
    availableSlots: slots,
    totalCapacity: restaurant.maxPartySize,
    fullyBooked: false,
  };
}

// ── Handlers ──────────────────────────────────────────────────────────────────

export const bookingsHandlers = [
  // GET /api/v1/restaurants — booking-service restaurant list (raw array)
  http.get("/api/v1/restaurants", async () => {
    await delay(60);
    return HttpResponse.json(MOCK_RESTAURANTS);
  }),

  // GET /api/v1/bookings/availability
  http.get("/api/v1/bookings/availability", async ({ request }) => {
    await delay(100);
    const url = new URL(request.url);
    const restaurantId = url.searchParams.get("restaurantId") ?? "";
    const date = url.searchParams.get("date") ?? "";
    const partySize = Number.parseInt(url.searchParams.get("partySize") ?? "2", 10);
    return HttpResponse.json(buildAvailability(restaurantId, date, partySize));
  }),

  // POST /api/v1/bookings — create booking (raw BookingResponse, 201)
  http.post("/api/v1/bookings", async ({ request }) => {
    await delay(150);
    const body = (await request.json()) as Record<string, unknown>;
    const restaurant =
      MOCK_RESTAURANTS.find((r) => r.id === body.restaurantId) ?? MOCK_RESTAURANTS[0];

    const reservationTime = (body.reservationTime as string) ?? relIso(1, 19);
    const endHour = new Date(reservationTime);
    endHour.setMinutes(endHour.getMinutes() + 90);

    const newBooking: BookingResponse = {
      id: `bb${Date.now()}-0000-0000-0000-000000000099`,
      bookingNumber: makeBookingNumber(),
      customerId: (body.customerId as string) ?? "customer-uuid-sub",
      customerName: (body.customerName as string) ?? "Guest",
      customerPhone: (body.customerPhone as string) ?? "",
      customerEmail: (body.customerEmail as string) ?? "",
      restaurantId: restaurant.id,
      restaurantName: restaurant.name,
      tableId: "tt000001-0000-0000-0000-000000000001",
      tableName: "Table A",
      tableType: (body.preferredTableType as TableType) ?? TableType.INDOOR,
      reservationTime,
      endTime: endHour.toISOString().replace(/\.\d{3}Z$/, ""),
      partySize: (body.partySize as number) ?? 2,
      status: BookingStatus.CONFIRMED,
      specialRequests: (body.specialRequests as string) ?? null,
      preOrderId: null,
      createdAt: new Date().toISOString().replace(/\.\d{3}Z$/, ""),
      updatedAt: new Date().toISOString().replace(/\.\d{3}Z$/, ""),
    };
    bookingStore.push(newBooking);
    return HttpResponse.json(newBooking, { status: 201 });
  }),

  // GET /api/v1/bookings/customer/:customerId
  http.get("/api/v1/bookings/customer/:customerId", async () => {
    await delay(80);
    return HttpResponse.json(bookingStore);
  }),

  // GET /api/v1/bookings/number/:bookingNumber
  http.get("/api/v1/bookings/number/:bookingNumber", async ({ params }) => {
    await delay(60);
    const booking = bookingStore.find(
      (b) => b.bookingNumber === params.bookingNumber,
    );
    if (!booking) {
      return HttpResponse.json({ error: "Booking not found" }, { status: 404 });
    }
    return HttpResponse.json(booking);
  }),

  // GET /api/v1/bookings/:bookingId
  http.get("/api/v1/bookings/:bookingId", async ({ params }) => {
    await delay(60);
    const booking = bookingStore.find((b) => b.id === params.bookingId);
    if (!booking) {
      return HttpResponse.json({ error: "Booking not found" }, { status: 404 });
    }
    return HttpResponse.json(booking);
  }),

  // POST /api/v1/bookings/:bookingId/cancel
  http.post("/api/v1/bookings/:bookingId/cancel", async ({ params }) => {
    await delay(120);
    const idx = bookingStore.findIndex((b) => b.id === params.bookingId);
    if (idx === -1) {
      return HttpResponse.json({ error: "Booking not found" }, { status: 404 });
    }
    bookingStore[idx] = {
      ...bookingStore[idx],
      status: BookingStatus.CANCELLED,
      updatedAt: new Date().toISOString().replace(/\.\d{3}Z$/, ""),
    };
    return HttpResponse.json(bookingStore[idx]);
  }),
];
