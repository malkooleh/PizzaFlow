import { http, HttpResponse } from "msw";

export const catalogHandlers = [
  http.get("*/api/v1/catalog/menu/:restaurantId", () => {
    return HttpResponse.json({
      success: true,
      data: [],
      message: "Menu retrieved successfully",
      error: null,
      timestamp: new Date().toISOString(),
      traceId: "mock-trace-id",
    });
  }),

  http.get("*/api/v1/restaurants", () => {
    return HttpResponse.json({
      success: true,
      data: [
        {
          id: "550e8400-e29b-41d4-a716-446655440001",
          name: "PizzaFlow Downtown",
          address: "123 Main Street",
          phone: "+1 555-0100",
          openingTime: "10:00",
          closingTime: "22:00",
          isActive: true,
        },
      ],
      message: "Restaurants retrieved",
      error: null,
      timestamp: new Date().toISOString(),
      traceId: "mock-trace-id",
    });
  }),
];
