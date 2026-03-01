import { http, HttpResponse } from "msw";
import type { ApiResponse } from "@/api/types";
import type { MenuItem, Restaurant } from "@/types/models";
import { MenuCategory } from "@/types/enums";

const MOCK_RESTAURANTS: Restaurant[] = [
  {
    id: "rest-1",
    name: "Napoli Central",
    address: "12 Margherita Street, Rome",
    phone: "+39 06 1234567",
    openingTime: "11:00",
    closingTime: "23:00",
    isActive: true,
    imageUrl: undefined,
  },
  {
    id: "rest-2",
    name: "Pizza Roma",
    address: "88 Colosseo Ave, Rome",
    phone: "+39 06 9876543",
    openingTime: "10:00",
    closingTime: "22:00",
    isActive: true,
    imageUrl: undefined,
  },
];

const MOCK_MENU_ITEMS: MenuItem[] = [
  {
    id: "item-1",
    restaurantId: "rest-1",
    name: "Margherita",
    description: "Classic tomato, fresh mozzarella, and basil",
    price: 12.5,
    category: MenuCategory.PIZZA,
    isAvailable: true,
    isFeatured: true,
    isVegetarian: true,
    isVegan: false,
    isGlutenFree: false,
    ingredients: ["Tomato sauce", "Mozzarella", "Basil"],
    allergens: ["Gluten", "Dairy"],
    calories: 780,
    preparationTimeMinutes: 15,
    modifierGroups: [
      {
        name: "Size",
        required: true,
        maxSelections: 1,
        options: [
          { modifierId: "size-s", name: "Small (25cm)", additionalPrice: 0 },
          { modifierId: "size-m", name: "Medium (32cm)", additionalPrice: 3 },
          { modifierId: "size-l", name: "Large (40cm)", additionalPrice: 6 },
        ],
      },
    ],
  },
  {
    id: "item-2",
    restaurantId: "rest-1",
    name: "Diavola",
    description: "Spicy salami, chilli flakes, smoked mozzarella",
    price: 14.0,
    category: MenuCategory.PIZZA,
    isAvailable: true,
    isFeatured: false,
    isVegetarian: false,
    isVegan: false,
    isGlutenFree: false,
    ingredients: ["Tomato sauce", "Smoked mozzarella", "Salami piccante", "Chilli"],
    allergens: ["Gluten", "Dairy"],
    calories: 920,
    preparationTimeMinutes: 18,
    modifierGroups: [],
  },
  {
    id: "item-3",
    restaurantId: "rest-1",
    name: "Tiramisu",
    description: "Classic Italian dessert with espresso and mascarpone",
    price: 6.5,
    category: MenuCategory.DESSERT,
    isAvailable: true,
    isFeatured: true,
    isVegetarian: true,
    isVegan: false,
    isGlutenFree: false,
    ingredients: ["Mascarpone", "Espresso", "Savoiardi", "Eggs", "Cocoa"],
    allergens: ["Dairy", "Eggs", "Gluten"],
    calories: 340,
    preparationTimeMinutes: 5,
    modifierGroups: [],
  },
  {
    id: "item-4",
    restaurantId: "rest-1",
    name: "San Pellegrino",
    description: "Sparkling mineral water 750ml",
    price: 3.0,
    category: MenuCategory.DRINK,
    isAvailable: true,
    isFeatured: false,
    isVegetarian: true,
    isVegan: true,
    isGlutenFree: true,
    ingredients: [],
    allergens: [],
    preparationTimeMinutes: 1,
    modifierGroups: [],
  },
];

function success<T>(data: T, message = "OK"): ApiResponse<T> {
  return {
    success: true,
    data,
    message,
    error: null,
    timestamp: new Date().toISOString(),
    traceId: "mock-trace-001",
  };
}

export const menuHandlers = [
  // List all restaurants
  http.get("/api/v1/restaurants", () =>
    HttpResponse.json(success(MOCK_RESTAURANTS))
  ),

  // Single restaurant
  http.get("/api/v1/restaurants/:id", ({ params }) => {
    const restaurant = MOCK_RESTAURANTS.find((r) => r.id === params["id"]);
    if (!restaurant) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json(success(restaurant));
  }),

  // Full menu
  http.get("/api/v1/catalog/menu/:restaurantId", ({ params, request }) => {
    const url = new URL(request.url);
    const category = url.searchParams.get("category");
    let items = MOCK_MENU_ITEMS.filter((i) => i.restaurantId === params["restaurantId"]);
    if (category) items = items.filter((i) => i.category === category);
    return HttpResponse.json(success(items));
  }),

  // Featured items
  http.get("/api/v1/catalog/menu/:restaurantId/featured", ({ params }) => {
    const items = MOCK_MENU_ITEMS.filter(
      (i) => i.restaurantId === params["restaurantId"] && i.isFeatured
    );
    return HttpResponse.json(success(items));
  }),

  // Search
  http.get("/api/v1/catalog/menu/:restaurantId/search", ({ params, request }) => {
    const q = new URL(request.url).searchParams.get("query")?.toLowerCase() ?? "";
    const items = MOCK_MENU_ITEMS.filter(
      (i) =>
        i.restaurantId === params["restaurantId"] &&
        (i.name.toLowerCase().includes(q) || i.description.toLowerCase().includes(q))
    );
    return HttpResponse.json(success(items));
  }),

  // Single item
  http.get("/api/v1/catalog/items/:id", ({ params }) => {
    const item = MOCK_MENU_ITEMS.find((i) => i.id === params["id"]);
    if (!item) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json(success(item));
  }),
];
