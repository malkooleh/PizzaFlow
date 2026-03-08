# PizzaFlow — React Frontend Implementation Plan

> **Created:** February 28, 2026
> **Phase:** 4 Week 4 — React Frontend
> **Status:** 🚀 Ready for implementation

---

## Table of Contents

1. [Overview](#1-overview)
2. [Technology Stack](#2-technology-stack)
3. [Project Architecture](#3-project-architecture)
4. [Application Structure](#4-application-structure)
5. [Authentication & Authorization](#5-authentication--authorization)
6. [API Integration Layer](#6-api-integration-layer)
7. [State Management Strategy](#7-state-management-strategy)
8. [Implementation Sequences](#8-implementation-sequences)
9. [Infrastructure & DevOps](#9-infrastructure--devops)
10. [Testing Strategy](#10-testing-strategy)
11. [Key Technical Decisions](#11-key-technical-decisions)
12. [Appendix: API Reference Summary](#appendix-api-reference-summary)

---

## 1. Overview

### 1.1 Goals

Build a modern, production-grade **single-page application (SPA)** that serves all five PizzaFlow user roles through a unified codebase with role-based routing and feature gating.

| Role | Primary Views |
|------|---------------|
| **CUSTOMER** | Menu browsing, shopping cart, checkout, order tracking, table bookings, notification inbox |
| **KITCHEN_STAFF** | Kitchen Display System (KDS) — real-time WebSocket queue with order cards |
| **COURIER** | Active deliveries list, real-time route/map, status updates |
| **RESTAURANT_MANAGER** | Menu management (CRUD), daily analytics, table configuration, inventory overview |
| **SYSTEM_ADMIN** | Global operations cockpit: service health, cross-service analytics, incident/audit views, and platform governance |

### 1.2 Constraints & Integration Points

| Concern | Detail |
|---------|--------|
| **API Gateway** | `http://localhost:8080` — all REST calls go through the gateway |
| **Auth** | Keycloak OIDC (public client `pizzaflow-web`, PKCE S256, port 9090) |
| **WebSocket** | Kitchen service direct: `ws://localhost:8084/ws/kitchen` (STOMP/SockJS) |
| **Response wrapper** | Most services return `ApiResponse<T>`; booking/delivery/notification return raw DTOs |
| **ID types** | V1 APIs use `Long`; V2 + booking/delivery use `UUID` strings |
| **Dev port** | `4200` (matches Keycloak `pizzaflow-web` root URL) |

---

## 2. Technology Stack

### 2.1 Core

| Layer | Technology | Version | Rationale |
|-------|-----------|---------|-----------|
| **Language** | TypeScript | 5.7+ | Strict mode, type-safe API contracts |
| **UI library** | React | 19 | Concurrent features, React Compiler readiness |
| **Build tool** | Vite | 6.x | Fast HMR, native ESM, plugin ecosystem |
| **Package manager** | pnpm | 9.x | Strict, fast, efficient disk usage |

### 2.2 Routing & Data Fetching

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| **Routing** | TanStack Router v1 | Type-safe, file-based, built-in search params validation, loader patterns |
| **Server state** | TanStack Query v5 | Cache invalidation, optimistic updates, infinite scroll, devtools |
| **HTTP client** | ky | Tiny (< 3 KB), built on Fetch API, interceptors, retry, JSON by default |

### 2.3 UI & Styling

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| **Component library** | shadcn/ui | Copy-paste ownership, Radix UI accessibility, fully customizable |
| **Styling** | Tailwind CSS v4 | Zero-config CSS-first, `@theme` directive, Lightning CSS engine |
| **Icons** | Lucide React | Consistent icon set that pairs with shadcn/ui |
| **Charts** | Recharts 2.x | Composable, React-native charts for dashboards |
| **Maps** | react-map-gl + Mapbox GL JS | Delivery tracking; free tier sufficient for dev |
| **Toasts** | Sonner | Lightweight, beautiful, accessible notifications |
| **Date formatting** | date-fns v4 | Tree-shakeable, immutable, functional API |

### 2.4 Forms & Validation

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| **Forms** | React Hook Form v7 | Uncontrolled by default, minimal re-renders, great DX |
| **Schema validation** | Zod v3 | TypeScript-first schema validation, integrates with RHF via `@hookform/resolvers` |

### 2.5 State Management

| Scope | Technology | Rationale |
|-------|-----------|-----------|
| **Server state** | TanStack Query v5 | Canonical choice — cache, background refetch, devtools |
| **Client state** | Zustand v5 | Lightweight (< 1 KB), no boilerplate, persist middleware for cart |
| **URL state** | TanStack Router search params | Filter/sort/pagination in URL for shareability |

### 2.6 Real-Time & Auth

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| **Auth** | react-oidc-context + oidc-client-ts | Thin OIDC wrapper; works with any OIDC provider incl. Keycloak |
| **WebSocket** | @stomp/stompjs + sockjs-client | STOMP protocol for kitchen service; SockJS fallback |

### 2.7 Code Quality & Testing

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| **Linting** | ESLint v9 (flat config) + typescript-eslint | Modern flat config, type-aware rules |
| **Formatting** | Prettier 3 | Consistent formatting, integrated with ESLint |
| **Unit tests** | Vitest + React Testing Library | Vite-native, Jest-compatible API, fast |
| **E2E tests** | Playwright | Cross-browser, reliable, parallel execution |
| **API mocking** | MSW v2 (Mock Service Worker) | Intercepts at network level; shared mocks for dev + test |

---

## 3. Project Architecture

### 3.1 Folder Structure

```
frontend/
├── public/
│   ├── favicon.ico
│   └── assets/                   # Static images, fonts
├── src/
│   ├── main.tsx                  # App entry point
│   ├── app.tsx                   # Root component (providers, router)
│   ├── router.tsx                # TanStack Router instance
│   │
│   ├── api/                      # API integration layer
│   │   ├── client.ts             # Configured ky instance with auth interceptor
│   │   ├── types.ts              # ApiResponse<T>, shared API types
│   │   ├── catalog.api.ts        # Catalog service endpoints
│   │   ├── orders.api.ts         # Order service endpoints (V1 + V2)
│   │   ├── payments.api.ts       # Payment service endpoints
│   │   ├── bookings.api.ts       # Booking service endpoints
│   │   ├── kitchen.api.ts        # Kitchen service REST endpoints
│   │   ├── deliveries.api.ts     # Delivery service endpoints
│   │   ├── notifications.api.ts  # Notification service endpoints
│   │   ├── admin.api.ts          # Admin/operations dashboard aggregation endpoints
│   │   └── restaurants.api.ts    # Restaurant service endpoints
│   │
│   ├── hooks/                    # TanStack Query hooks (per domain)
│   │   ├── use-menu.ts           # useMenu, useMenuItem, useSearchMenu
│   │   ├── use-orders.ts         # useOrders, useCreateOrder, useOrderTracking
│   │   ├── use-payments.ts       # useProcessPayment, usePaymentHistory
│   │   ├── use-bookings.ts       # useAvailability, useCreateBooking, useBookings
│   │   ├── use-kitchen.ts        # useKitchenQueue, useKitchenWebSocket
│   │   ├── use-deliveries.ts     # useDeliveries, useDeliveryTracking
│   │   ├── use-admin.ts          # useSystemHealth, useBusinessKPIs, useAuditFeed, useAlerts
│   │   └── use-notifications.ts  # useNotifications, useUnreadCount
│   │
│   ├── stores/                   # Zustand stores (client-only state)
│   │   ├── cart.store.ts         # Shopping cart (persisted to localStorage)
│   │   ├── ui.store.ts           # Theme, sidebar collapsed, restaurant selection
│   │   └── kds.store.ts          # KDS display preferences (layout, alerts)
│   │
│   ├── components/               # Shared/reusable components
│   │   ├── ui/                   # shadcn/ui primitives (button, dialog, etc.)
│   │   ├── layout/               # Shell, Sidebar, Header, Footer, RoleGuard
│   │   ├── auth/                 # ProtectedRoute, LoginButton, UserMenu
│   │   ├── feedback/             # LoadingSpinner, ErrorBoundary, EmptyState
│   │   └── common/               # PriceTag, StatusBadge, OrderTimeline, etc.
│   │
│   ├── features/                 # Feature modules (domain-specific UI)
│   │   ├── menu/                 # MenuGrid, MenuItemCard, MenuItemDetail
│   │   │   ├── components/
│   │   │   └── utils/
│   │   ├── cart/                 # CartSheet, CartItem, CartSummary
│   │   │   └── components/
│   │   ├── checkout/             # CheckoutForm, PaymentMethodSelector, OrderReview
│   │   │   └── components/
│   │   ├── orders/               # OrderList, OrderDetail, OrderTimeline, OrderTracker
│   │   │   └── components/
│   │   ├── bookings/             # AvailabilityCalendar, BookingForm, BookingList
│   │   │   └── components/
│   │   ├── kitchen/              # KDSBoard, OrderCard, StationColumn, TimerBadge
│   │   │   └── components/
│   │   ├── delivery/             # DeliveryMap, DeliveryList, CourierControls
│   │   │   └── components/
│   │   ├── manager/              # MenuEditor, AnalyticsDashboard, TableConfig
│   │   │   └── components/
│   │   ├── admin/                # AdminOverview, ServiceHealthGrid, OpsTimeline, AuditFeed, AlertCenter
│   │   │   └── components/
│   │   └── notifications/        # NotificationInbox, NotificationBell, PreferencesPanel
│   │       └── components/
│   │
│   ├── routes/                   # TanStack Router file-based route definitions
│   │   ├── __root.tsx            # Root layout (Shell)
│   │   ├── index.tsx             # Landing / home
│   │   ├── login.tsx             # Login redirect
│   │   ├── menu/
│   │   │   ├── index.tsx         # Menu browsing page
│   │   │   └── $itemId.tsx       # Menu item detail page
│   │   ├── cart.tsx              # Cart page (mobile full-page view)
│   │   ├── checkout.tsx          # Checkout page
│   │   ├── orders/
│   │   │   ├── index.tsx         # My orders list
│   │   │   └── $orderId.tsx      # Order detail + tracking
│   │   ├── bookings/
│   │   │   ├── index.tsx         # My bookings list
│   │   │   └── new.tsx           # New booking form
│   │   ├── kitchen/
│   │   │   └── index.tsx         # KDS board (KITCHEN_STAFF)
│   │   ├── courier/
│   │   │   └── index.tsx         # Active deliveries (COURIER)
│   │   ├── manager/
│   │   │   ├── index.tsx         # Manager dashboard
│   │   │   ├── menu.tsx          # Menu management CRUD
│   │   │   ├── bookings.tsx      # Today's bookings view
│   │   │   ├── analytics.tsx     # Order analytics
│   │   │   └── inventory.tsx     # Stock levels
│   │   └── admin/
│   │       ├── index.tsx         # Admin overview dashboard
│   │       ├── services.tsx      # Service health + incidents
│   │       ├── analytics.tsx     # Cross-service business analytics
│   │       ├── audit.tsx         # Audit/event activity timeline
│   │       └── alerts.tsx        # Alert center and acknowledgment flow
│   │
│   ├── lib/                      # Shared utilities
│   │   ├── auth.ts               # OIDC config, role helpers, token utils
│   │   ├── websocket.ts          # STOMP client setup + reconnection
│   │   ├── format.ts             # Currency, date, address formatting
│   │   ├── constants.ts          # API URLs, role constants, order status colors
│   │   └── schemas.ts            # Shared Zod schemas (reused across forms + API)
│   │
│   ├── types/                    # Global TypeScript types/interfaces
│   │   ├── models.ts             # Domain models (Order, MenuItem, Booking, etc.)
│   │   ├── enums.ts              # OrderStatus, PaymentStatus, etc.
│   │   └── api.ts                # ApiResponse<T>, pagination types
│   │
│   └── styles/
│       └── globals.css           # Tailwind v4 @theme + @import, CSS custom properties
│
├── .env                          # VITE_API_URL, VITE_KEYCLOAK_URL, etc.
├── .env.development              # Dev-specific overrides
├── components.json               # shadcn/ui configuration
├── eslint.config.js              # ESLint v9 flat config
├── index.html                    # Vite entry HTML
├── package.json
├── pnpm-lock.yaml
├── postcss.config.js             # PostCSS (Tailwind v4)
├── tailwind.config.ts            # Tailwind theme customization
├── tsconfig.json
├── tsconfig.app.json
├── vite.config.ts
└── vitest.config.ts
```

### 3.2 Key Architectural Principles

1. **Feature-sliced design** — UI logic is organized by business domain (`features/`), not by technical role (no `containers/` vs `presentational/` split).
2. **Colocation** — Components, hooks, and utilities that belong to a single feature live inside that feature's folder.
3. **API layer as the single source of truth** — All HTTP calls go through `api/*.api.ts` files. TanStack Query hooks in `hooks/` wrap these. No `fetch()` calls in components.
4. **Server state ≠ client state** — TanStack Query owns server data (orders, menu). Zustand owns purely client-side state (cart contents, UI preferences).
5. **Type-safe from API to UI** — Zod schemas validate API responses at runtime; TypeScript types are inferred from Zod, ensuring the API contract is enforced at the boundary.
6. **URL as state** — Filters, pagination, and search terms are stored in URL search params via TanStack Router, making views bookmarkable and shareable.

---

## 4. Application Structure

### 4.1 Layout System

The app uses a **Shell layout** pattern with role-aware navigation:

```
┌─────────────────────────────────────────────┐
│  Header (logo, search, cart, notifications, │
│          user menu with role badge)          │
├──────────┬──────────────────────────────────┤
│          │                                  │
│ Sidebar  │        Main Content Area         │
│ (role-   │     (route-specific views)       │
│  aware   │                                  │
│  nav)    │                                  │
│          │                                  │
├──────────┴──────────────────────────────────┤
│  Footer (customer pages only)               │
└─────────────────────────────────────────────┘
```

**Navigation items by role:**

| Role | Sidebar Items |
|------|--------------|
| CUSTOMER | Menu, My Orders, Bookings, Notifications |
| KITCHEN_STAFF | Kitchen Board, Queue History |
| COURIER | Active Deliveries, Delivery History |
| RESTAURANT_MANAGER | Dashboard, Menu Management, Bookings, Inventory, Analytics |
| SYSTEM_ADMIN | Admin Overview, Services Health, Global Analytics, Audit Feed, Alert Center |

### 4.2 Responsive Strategy

- **Mobile-first** — Tailwind's `sm:`, `md:`, `lg:` breakpoints.
- **Sidebar** → bottom tab bar on mobile for Customer/Courier views.
- **Cart** → Sheet (drawer) on desktop, full page on mobile.
- **KDS** → optimized for landscape tablets (kitchen screens).

---

## 5. Authentication & Authorization

### 5.1 OIDC Configuration

```typescript
// src/lib/auth.ts — OIDC config for react-oidc-context
const oidcConfig = {
  authority: "http://localhost:9090/realms/pizzaflow",
  client_id: "pizzaflow-web",
  redirect_uri: "http://localhost:4200/callback",
  post_logout_redirect_uri: "http://localhost:4200",
  scope: "openid profile email roles",
  response_type: "code",
  // PKCE is enforced server-side (S256)
};
```

### 5.2 Auth Flow

1. User clicks **Sign In** → redirected to Keycloak login page.
2. After auth, Keycloak redirects back to `/callback` with auth code.
3. `oidc-client-ts` exchanges code for tokens (PKCE S256).
4. Access token is stored in memory (not localStorage for security).
5. `ky` HTTP client attaches `Authorization: Bearer <token>` to every API call via beforeRequest hook.
6. Token refresh handled automatically by `oidc-client-ts` (silent renewal via iframe).
7. On 401 response → trigger silent refresh; if that fails → redirect to login.

### 5.3 Role-Based Access Control

```typescript
// Route-level protection via TanStack Router's beforeLoad
export const Route = createFileRoute("/kitchen/")({
  beforeLoad: ({ context }) => {
    requireRole(context.auth, ["KITCHEN_STAFF", "RESTAURANT_MANAGER", "SYSTEM_ADMIN"]);
  },
});

// Component-level with <RoleGuard>
<RoleGuard roles={["RESTAURANT_MANAGER"]}>
  <MenuEditorPanel />
</RoleGuard>
```

**Role extraction:** Keycloak puts realm roles in the JWT at `realm_access.roles`. The OIDC user profile is extended to include parsed roles.

### 5.4 Guest Access

- Menu browsing (`/menu`) is **public** — no auth required.
- Cart is available to guests (persisted in `localStorage`).
- Checkout requires authentication → prompt login if unauthenticated.
- KDS, Courier, Manager routes **always** require auth + specific role.

---

## 6. API Integration Layer

### 6.1 HTTP Client Setup

```typescript
// src/api/client.ts
import ky from "ky";

export const api = ky.create({
  prefixUrl: import.meta.env.VITE_API_URL, // "http://localhost:8080"
  timeout: 15_000,
  hooks: {
    beforeRequest: [
      (request) => {
        const token = getAccessToken(); // from OIDC context
        if (token) {
          request.headers.set("Authorization", `Bearer ${token}`);
        }
      },
    ],
    afterResponse: [
      async (_request, _options, response) => {
        if (response.status === 401) {
          await silentRefresh();
        }
      },
    ],
  },
});
```

### 6.2 Response Normalization

Some services wrap responses in `ApiResponse<T>`, others return raw DTOs. The API layer normalizes this:

```typescript
// src/api/types.ts
interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string | null;
  error: ErrorDetails | null;
  timestamp: string;
  traceId: string | null;
}

interface ErrorDetails {
  code: string | null;
  field: string | null;
  rejectedValue: any;
}

// Unwrap helper
function unwrap<T>(response: ApiResponse<T>): T {
  if (!response.success || response.data === null) {
    throw new ApiError(response.message, response.error);
  }
  return response.data;
}
```

Services that return `ApiResponse<T>` (order, catalog, payment, kitchen, inventory) use `unwrap()`. Services that return raw DTOs (booking, delivery, notification) return the response directly.

### 6.3 API Module Example

```typescript
// src/api/catalog.api.ts
export const catalogApi = {
  getMenu: (restaurantId: string) =>
    api.get(`api/v1/catalog/menu/${restaurantId}`).json<ApiResponse<MenuItem[]>>().then(unwrap),

  getMenuItem: (id: string) =>
    api.get(`api/v1/catalog/items/${id}`).json<ApiResponse<MenuItem>>().then(unwrap),

  searchMenu: (restaurantId: string, query: string) =>
    api.get(`api/v1/catalog/menu/${restaurantId}/search`, { searchParams: { query } })
      .json<ApiResponse<MenuItem[]>>().then(unwrap),
};
```

### 6.4 TanStack Query Hook Example

```typescript
// src/hooks/use-menu.ts
export function useMenu(restaurantId: string) {
  return useQuery({
    queryKey: ["menu", restaurantId],
    queryFn: () => catalogApi.getMenu(restaurantId),
    staleTime: 5 * 60 * 1000, // Menu changes infrequently
  });
}
```

---

## 7. State Management Strategy

### 7.1 State Categories

| State Type | Tool | Examples |
|-----------|------|---------|
| **Server state** | TanStack Query | Orders, menu items, bookings, kitchen queue |
| **Client state** | Zustand (persisted) | Shopping cart, selected restaurant, theme |
| **URL state** | TanStack Router search params | Filters, sort, page, search query |
| **Auth state** | react-oidc-context | User profile, tokens, roles |
| **Real-time state** | TanStack Query + WebSocket | KDS queue (WS pushes invalidate query cache) |

### 7.2 Cart Store (Zustand)

```typescript
// src/stores/cart.store.ts
interface CartState {
  items: CartItem[];
  restaurantId: string | null;
  // Actions
  addItem: (item: CartItem) => void;
  removeItem: (itemId: string) => void;
  updateQuantity: (itemId: string, quantity: number) => void;
  clear: () => void;
}

// Persisted to localStorage via zustand/middleware
export const useCartStore = create<CartState>()(
  persist(/* ... */, { name: "pizzaflow-cart" })
);
```

**Cart rule:** All items must belong to the same restaurant. Adding an item from a different restaurant prompts confirmation to clear the cart.

### 7.3 WebSocket → Query Integration (KDS)

WebSocket events from the kitchen service invalidate TanStack Query cache, triggering re-renders with fresh data:

```typescript
// In KDS page: WebSocket message handler
stompClient.subscribe(`/topic/kitchen/${restaurantId}`, (message) => {
  const update = JSON.parse(message.body);
  queryClient.invalidateQueries({ queryKey: ["kitchen-queue", restaurantId] });
  // Also show toast for NEW_ORDER and STATUS_CHANGE events
});
```

---

## 8. Implementation Sequences

The frontend is built in **8 sequential implementation sequences**. Each sequence is self-contained, testable, and delivers a reviewable milestone. After completing and reviewing each sequence, we proceed to the next.

---

### Sequence 1: Project Foundation & Auth

**Goal:** Scaffolded project with working authentication, layout shell, and dev tooling.

**Scope:**
- Initialize Vite + React 19 + TypeScript project
- Install and configure: Tailwind CSS v4, shadcn/ui, TanStack Router, TanStack Query, ESLint, Prettier
- Set up `pnpm` as package manager
- Create environment files (`.env`, `.env.development`) with `VITE_API_URL`, `VITE_KEYCLOAK_URL`, `VITE_KEYCLOAK_CLIENT_ID`
- Configure Vite dev server on port 4200 with API proxy to `localhost:8080`
- Initialize shadcn/ui (`components.json`, base primitives: Button, Card, Dialog, Sheet, Dropdown, Avatar, Badge, Separator, Skeleton, Tabs, Tooltip)
- Implement OIDC auth flow with `react-oidc-context`:
  - `AuthProvider` wrapping the app
  - Login / logout functionality
  - Token attachment to API calls via `ky` interceptor
  - Role extraction from JWT (`realm_access.roles`)
  - `<ProtectedRoute>` component and `<RoleGuard>` component
- Create application Shell layout:
  - Responsive `<Header>` with logo, user menu (avatar + role badge), login/logout button
  - `<Sidebar>` with role-aware navigation (collapsible on desktop, bottom tabs on mobile)
  - `<Footer>` for customer pages
- Set up TanStack Router:
  - Root route with Shell layout
  - Landing page (`/`) with hero section and restaurant selector
  - Login callback route (`/callback`)
  - 404 catch-all route
- Set up TanStack Query `QueryClientProvider` with React Query DevTools
- Set up Sonner `<Toaster>` for notifications
- Create `api/client.ts` with configured `ky` instance
- Add CORS configuration to API Gateway (`application.yml`) to allow `http://localhost:4200`
- Add frontend service to `docker-compose.services.yml` (Dockerfile with nginx)

**Files created:** ~25–30 files (project scaffold, layout components, auth setup)

**Deliverable:** Running app at `localhost:4200` — login with Keycloak, see role-based sidebar, empty content area.

---

### Sequence 2: Menu Browsing & Restaurant Selection

**Goal:** Customer can browse restaurant menus, search, filter by category, view item details.

**Scope:**
- Create TypeScript types for `MenuItem`, `Modifier`, `Recipe`, menu-related enums (`src/types/`)
- Create Zod schemas for API response validation
- Implement `api/restaurants.api.ts` and `api/catalog.api.ts`
- Implement TanStack Query hooks: `useRestaurants`, `useMenu`, `useMenuByCategory`, `useFeaturedItems`, `useSearchMenu`, `useMenuItem`
- Create Zustand `ui.store.ts` for selected restaurant (persisted)
- Build feature components (`features/menu/`):
  - `<RestaurantSelector>` — dropdown or card grid of available restaurants
  - `<MenuGrid>` — responsive grid of menu item cards with skeleton loading
  - `<MenuItemCard>` — image, name, description, price, dietary tags, "Add to Cart" button
  - `<MenuItemDetail>` — full detail view with modifiers (size, toppings), nutritional info, allergens
  - `<CategoryTabs>` — horizontal scrollable tabs (PIZZA, DRINK, SIDE, DESSERT)
  - `<MenuSearch>` — search input with debounced query (URL search param)
  - `<DietaryFilter>` — filter pills (Vegetarian, Vegan, Gluten-Free)
  - `<PriceTag>` — formatted currency display
- Create routes:
  - `/menu` — restaurant selection + menu browsing
  - `/menu/$itemId` — menu item detail page
- Implement URL-based search params for filters (`?category=PIZZA&q=margherita&dietary=VEGETARIAN`)
- Add `<EmptyState>` component for no results
- MSW mock handlers for catalog API (dev-time only)

**Deliverable:** Fully functional menu browsing with search, category filter, and item detail view.

---

### Sequence 3: Shopping Cart & Checkout

**Goal:** Customer can add items to cart, customize modifiers, and complete checkout with payment.

**Scope:**
- Implement Zustand `cart.store.ts` with `localStorage` persistence:
  - Add/remove/update items with modifier selections
  - Subtotal, tax (10%), delivery fee calculation
  - Restaurant lock (one restaurant per cart)
  - Cart item count badge on header
- Create TypeScript types for order and payment DTOs (`CreateOrderRequest`, `PaymentRequest`, etc.)
- Implement `api/orders.api.ts` and `api/payments.api.ts`
- Implement hooks: `useCreateOrder` (mutation), `useProcessPayment` (mutation)
- Build feature components (`features/cart/`):
  - `<CartSheet>` — slide-out drawer (desktop) showing cart contents
  - `<CartItem>` — item row with quantity stepper, modifier summary, remove button
  - `<CartSummary>` — subtotal, tax, delivery fee, total
  - `<CartBadge>` — item count indicator on header cart icon
- Build feature components (`features/checkout/`):
  - `<CheckoutPage>` — multi-step or single-page checkout
  - `<OrderTypeSelector>` — DELIVERY / PICKUP / DINE_IN toggle
  - `<DeliveryAddressForm>` — address input with validation (Zod + React Hook Form)
  - `<TableNumberInput>` — for DINE_IN orders
  - `<ScheduledTimeSelector>` — date/time picker for SCHEDULED orders
  - `<PaymentMethodSelector>` — card type selection (CREDIT_CARD, DEBIT_CARD, PAYPAL, etc.)
  - `<PaymentForm>` — card number/expiry/cvv fields (mock — simulated gateway)
  - `<OrderReview>` — final summary before "Place Order"
  - `<OrderConfirmation>` — success screen with order number and link to tracking
- Create routes:
  - `/cart` — full-page cart (mobile)
  - `/checkout` — requires auth (redirects to login if guest)
- Implement optimistic UI updates for cart operations
- Error handling: display `ApiResponse.error` messages in toast notifications

**Deliverable:** Full purchase flow — add items → review cart → checkout → payment → order confirmed.

---

### Sequence 4: Order Tracking & History

**Goal:** Customer can view order history, track active orders in real-time, and cancel pending orders.

**Scope:**
- Implement additional hooks: `useOrders` (customer orders, paginated), `useOrder` (single), `useOrderByNumber`, `useCancelOrder` (mutation)
- Build feature components (`features/orders/`):
  - `<OrderList>` — paginated list with status badges, date, total
  - `<OrderDetail>` — full order info with items, pricing breakdown, timestamps
  - `<OrderTimeline>` — visual step progression (PENDING → CONFIRMED → PREPARING → READY → DELIVERED/COMPLETED)
  - `<OrderTracker>` — polling-based live status (5s interval for active orders)
  - `<StatusBadge>` — color-coded (`PENDING`=yellow, `PREPARING`=blue, `READY`=green, `CANCELLED`=red)
  - `<CancelOrderDialog>` — confirmation dialog with reason input
  - `<ReorderButton>` — pre-fill cart from previous order
- Create routes:
  - `/orders` — my orders list (requires auth)
  - `/orders/$orderId` — order detail + tracking
- Implement search params: `?status=PREPARING&page=1&size=10`
- Auto-refetch active orders via `refetchInterval` in TanStack Query

**Deliverable:** Customer can track orders from placement to completion with visual timeline.

---

### Sequence 5: Table Bookings

**Goal:** Customer can check table availability, make reservations, view/cancel bookings.

**Scope:**
- Create TypeScript types for booking models (`BookingRequest`, `BookingResponse`, `AvailabilitySlot`, etc.)
- Implement `api/bookings.api.ts`
- Implement hooks: `useAvailability`, `useCreateBooking`, `useBookings` (customer), `useCancelBooking`, `useBookingByNumber`
- Build feature components (`features/bookings/`):
  - `<AvailabilityCalendar>` — date picker + time slot grid showing available/unavailable slots
  - `<BookingForm>` — party size, date, time slot, guest name/phone/email, special requests (React Hook Form + Zod)
  - `<TableTypePreference>` — optional preference (INDOOR, OUTDOOR, BAR, PRIVATE, VIP)
  - `<BookingList>` — customer's bookings with status badges
  - `<BookingDetail>` — full info including linked order (if hybrid)
  - `<BookingConfirmation>` — success screen with booking number
  - `<CancelBookingDialog>` — confirmation with reason
- Create routes:
  - `/bookings` — my bookings list (requires auth)
  - `/bookings/new` — new booking form
- Restaurant selector integration — bookings are per-restaurant

**Deliverable:** End-to-end booking flow — check availability → fill form → confirm → view in list.

---

### Sequence 6: Kitchen Display System (KDS)

**Goal:** Kitchen staff sees real-time order queue with WebSocket updates, can transition order status.

**Scope:**
- Implement `lib/websocket.ts` — STOMP client setup with auto-reconnection
- Implement `api/kitchen.api.ts` REST calls (for initial load and actions)
- Implement hooks:
  - `useKitchenQueue` — REST query for initial queue load
  - `useKitchenWebSocket` — subscribes to `/topic/kitchen/{restaurantId}`, invalidates query cache on updates
  - `useStartPreparing`, `useMarkReady`, `useMarkPickedUp` — mutations
- Build feature components (`features/kitchen/`):
  - `<KDSBoard>` — multi-column Kanban-style board: RECEIVED | PREPARING | READY
  - `<KDSOrderCard>` — order card with: order number, items list, order type badge, priority indicator, elapsed timer, action button
  - `<StationColumn>` — column with header showing count, scrollable card list
  - `<TimerBadge>` — real-time elapsed time since order received (color changes: green < 10min, yellow < 20min, red > 20min)
  - `<QueueStats>` — top bar showing total orders, avg wait time, orders by status
  - `<OrderPriorityIndicator>` — visual priority (URGENT=red pulse, HIGH=orange, NORMAL=default, LOW=muted)
  - `<KDSSettings>` — display preferences (column layout, auto-scroll, alert sounds)
- Create route:
  - `/kitchen` — requires KITCHEN_STAFF or RESTAURANT_MANAGER role
- Implement audio alert for new orders (configurable)
- Optimized for landscape tablet resolution (1280x800)
- Auto-reconnect WebSocket with exponential backoff

**Deliverable:** Real-time kitchen dashboard — cards flow from RECEIVED → PREPARING → READY as staff clicks through.

---

### Sequence 7: Courier, Manager & Admin Operations

**Goal:** Courier can manage deliveries; Manager can manage menu and restaurant analytics; System Admin can operate a global control panel with dashboards and incident awareness.

**Scope — Courier:**
- Implement `api/deliveries.api.ts` and `api/couriers.api.ts` (combined from delivery-service)
- Implement hooks: `useCourierDeliveries`, `useDeliveryTracking`, `useUpdateDeliveryStatus`, `useUpdateCourierLocation`, `useCourierOnline/Offline`
- Build feature components (`features/delivery/`):
  - `<DeliveryList>` — active deliveries with status, customer info, address
  - `<DeliveryDetail>` — order items, pickup/delivery addresses, timing
  - `<DeliveryMap>` — Mapbox map showing restaurant → delivery route with courier position
  - `<DeliveryStatusFlow>` — action buttons: Pick Up → In Transit → Arrived → Complete
  - `<CourierToggle>` — online/offline toggle with GPS permission request
  - `<LocationTracker>` — background GPS reporting (every 10s when on delivery)
- Create route:
  - `/courier` — requires COURIER role

**Scope — Manager:**
- Implement `api/inventory.api.ts` (stock levels, low stock alerts)
- Implement hooks: `useCreateMenuItem` (mutation), `useUpdateMenuItem`, `useDeleteMenuItem`, `useStockLevels`, `useLowStockItems`, `useTodayBookings`, `useOrderStats`
- Build feature components (`features/manager/`):
  - `<ManagerDashboard>` — summary cards (today's orders, revenue, active bookings, low stock count) + Recharts graphs
  - `<MenuEditor>` — table/grid of menu items with inline edit, add new, delete
  - `<MenuItemForm>` — full form for creating/editing menu item (name, price, category, modifiers, allergens, image URL)
  - `<TodayBookings>` — list of today's reservations with status actions (confirm, seat, no-show)
  - `<InventoryOverview>` — stock levels table with low-stock highlighting
  - `<AnalyticsCharts>` — Recharts: orders over time (line), revenue by day (bar), order type distribution (pie), popular items (horizontal bar)
- Create routes:
  - `/manager` — dashboard (requires RESTAURANT_MANAGER)
  - `/manager/menu` — menu CRUD
  - `/manager/bookings` — today's bookings
  - `/manager/analytics` — advanced analytics
  - `/manager/inventory` — stock levels

**Scope — Admin (SYSTEM_ADMIN):**
- Implement `api/admin.api.ts` for aggregated read models consumed by admin views:
  - service health summary (gateway-level status + degraded services)
  - platform KPI snapshot (orders, revenue, conversion, delivery SLA, payment success rate)
  - event/audit activity feed (key cross-service state changes)
  - active alerts and acknowledgment actions
- Implement hooks: `useSystemHealth`, `useBusinessKPIs`, `usePlatformTrends`, `useAuditFeed`, `useActiveAlerts`, `useAcknowledgeAlert`
- Build feature components (`features/admin/`):
  - `<AdminOverview>` — top-level KPI cards + service status heatmap + incidents count
  - `<ServiceHealthGrid>` — per-service cards with uptime, p95 latency, error rate, and last heartbeat
  - `<PlatformAnalytics>` — cross-service charts (orders/revenue trend, kitchen throughput, delivery SLA trend, payment failure analysis)
  - `<OpsTimeline>` — chronological event timeline (order/payment/kitchen/delivery milestones)
  - `<AuditFeed>` — searchable recent audit entries with actor, action, timestamp, correlation id
  - `<AlertCenter>` — active/resolved alerts with severity, owner, acknowledge/resolution actions
  - `<DateRangeFilter>` + `<RestaurantFilter>` — shared global filters for admin dashboards
- Create routes:
  - `/admin` — admin overview
  - `/admin/services` — service health and incidents
  - `/admin/analytics` — global analytics dashboards
  - `/admin/audit` — platform audit feed
  - `/admin/alerts` — alert management
- Access control:
  - all `/admin/**` routes require `SYSTEM_ADMIN`
  - admin-only actions (alert acknowledge/resolve) are additionally checked in backend APIs

**Deliverable:** Courier can manage delivery lifecycle with map; Manager can perform CRUD and restaurant analytics; System Admin gets a production-grade operations cockpit with health dashboards, analytics, and alert workflows.

---

### Sequence 8: Notifications, Polish & Production Readiness

**Goal:** Notification inbox, global polish, accessibility audit, PWA setup, production deployment config.

**Scope — Notifications:**
- Implement `api/notifications.api.ts`
- Implement hooks: `useNotifications` (paginated), `useUnreadCount` (polled every 30s), `useMarkAsRead`, `useMarkAllRead`, `useNotificationPreferences`
- Build feature components (`features/notifications/`):
  - `<NotificationBell>` — header icon with unread count badge (red dot)
  - `<NotificationInbox>` — dropdown panel with notification list (timestamp, message, type icon)
  - `<NotificationPreferences>` — settings page for email/SMS/push channel toggles
- Integrate notification bell into header for all authenticated users

**Scope — Polish & UX:**
- Dark mode toggle (Tailwind `dark:` classes, persisted in Zustand)
- `<ErrorBoundary>` with fallback UI for unhandled errors
- Skeleton loading states for all data-driven pages
- Empty states with illustration and CTA for all list pages
- Form validation error messages with field highlights
- Responsive audit — test all pages at 375px, 768px, 1024px, 1440px breakpoints
- Keyboard navigation audit for all interactive elements
- Screen reader accessibility audit (ARIA labels, roles, focus management)
- Image lazy loading, component code splitting via `React.lazy()` + `Suspense`

**Scope — Production Readiness:**
- Production Dockerfile (multi-stage: Node build → nginx serve)
- nginx config with SPA routing fallback, gzip, security headers
- Add frontend to `docker-compose.services.yml` on port 4200
- Add Kubernetes deployment + service to `infrastructure/kubernetes/base/services/`
- Add to CI/CD build matrix in `.github/workflows/ci-cd.yml`
- Environment-specific configuration via build-time env vars
- Lighthouse audit targeting: Performance >= 90, Accessibility >= 95, Best Practices >= 95
- PWA manifest + service worker for offline menu caching (optional enhancement)

**Deliverable:** Polished, accessible, production-ready frontend application deployable to Kubernetes.

---

## 9. Infrastructure & DevOps

### 9.1 API Gateway CORS Configuration

Must be added to `services/api-gateway/src/main/resources/application.yml`:

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:4200"
              - "http://localhost:3000"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

### 9.2 Vite Dev Server Proxy

For development, Vite proxies API requests to avoid CORS during local dev:

```typescript
// vite.config.ts
export default defineConfig({
  server: {
    port: 4200,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
```

### 9.3 Docker — Production Build

```dockerfile
# Stage 1: Build
FROM node:22-alpine AS builder
WORKDIR /app
RUN corepack enable && corepack prepare pnpm@latest --activate
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
RUN pnpm build

# Stage 2: Serve
FROM nginx:1.27-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 9.4 nginx Configuration

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    # SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy (production: use K8s ingress instead)
    location /api/ {
        proxy_pass http://api-gateway:8080/api/;
    }

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN";
    add_header X-Content-Type-Options "nosniff";
    add_header X-XSS-Protection "1; mode=block";
    add_header Referrer-Policy "strict-origin-when-cross-origin";

    # Gzip
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
}
```

---

## 10. Testing Strategy

### 10.1 Test Pyramid

| Layer | Tool | Focus | Target |
|-------|------|-------|--------|
| **Unit** | Vitest | Utility functions, Zustand stores, formatters | > 80% of `lib/`, `stores/` |
| **Component** | Vitest + RTL | Individual components in isolation | Key interactive components |
| **Integration** | Vitest + RTL + MSW | Feature flows (add to cart, checkout, booking form) | Happy path + error states |
| **E2E** | Playwright | Full user journeys through the actual app | Critical paths (menu → cart → checkout → tracking) |

### 10.2 MSW for Development & Testing

MSW handlers serve double duty — they provide realistic API responses during development (when backend is not running) and serve as test fixtures:

```typescript
// src/mocks/handlers/catalog.handlers.ts
export const catalogHandlers = [
  http.get("*/api/v1/catalog/menu/:restaurantId", () => {
    return HttpResponse.json({
      success: true,
      data: mockMenuItems,
      message: "Menu retrieved",
      /* ... */
    });
  }),
];
```

### 10.3 Testing Conventions

- Test files colocated with source: `MenuItemCard.test.tsx` next to `MenuItemCard.tsx`
- Use `@testing-library/user-event` for realistic user interactions (not `fireEvent`)
- Prefer `getByRole`, `getByLabelText` over `getByTestId` (accessibility-first testing)
- Playwright tests in `frontend/e2e/` directory

---

## 11. Key Technical Decisions

| # | Decision | Chosen | Alternatives Considered | Rationale |
|---|----------|--------|------------------------|-----------|
| 1 | **Routing** | TanStack Router | React Router v7 | Type-safe params/search, file-based, built-in loader support |
| 2 | **HTTP client** | ky | axios, ofetch | Tiny footprint, Fetch-based, hooks API for interceptors |
| 3 | **Auth approach** | react-oidc-context | @react-keycloak/web, custom hooks | Provider-agnostic OIDC; not locked into Keycloak dependency |
| 4 | **Component lib** | shadcn/ui | Radix directly, Headless UI | Pre-styled but ownable; Radix accessibility built in |
| 5 | **Client state** | Zustand | Redux Toolkit, Jotai | Minimal API for cart/UI; no action creators boilerplate |
| 6 | **Server state** | TanStack Query | SWR, RTK Query | Superior devtools, mutation support, infinite queries |
| 7 | **Forms** | React Hook Form + Zod | Formik + Yup | Less re-renders, TypeScript-first validation schemas |
| 8 | **Charts** | Recharts | Victory, Nivo, Chart.js | Most used React charting library; composable API |
| 9 | **Maps** | react-map-gl | Leaflet, Google Maps | Beautiful default tiles, free dev tier, GeoJSON support |
| 10 | **Package manager** | pnpm | npm, yarn | Strict mode prevents phantom deps; efficient disk usage |
| 11 | **Testing** | Vitest + RTL + Playwright | Jest + Cypress | Vite-native speed; Playwright more reliable cross-browser |
| 12 | **API mocking** | MSW v2 | Mirage JS, json-server | Network-level interception; same mocks for dev + test |

---

## Appendix: API Reference Summary

### Gateway Route → Service Mapping

| Frontend calls | Gateway route | Backend service | Port |
|----------------|---------------|-----------------|------|
| `/api/v1/catalog/**` | `lb://catalog-service` | catalog-service | 8082 |
| `/api/v1/orders/**` | `lb://order-service` | order-service | 8081 |
| `/api/v2/orders/**` | `lb://order-service` | order-service | 8081 |
| `/api/v1/payments/**` | `lb://payment-service` | payment-service | 8083 |
| `/api/v1/kitchen/**` | `lb://kitchen-service` | kitchen-service | 8084 |
| `/api/v1/bookings/**` | `lb://booking-service` | booking-service | 8086 |
| `/api/v1/restaurants/**` | `lb://booking-service` | booking-service | 8086 |
| `/api/v1/deliveries/**` | `lb://delivery-service` | delivery-service | 8087 |
| `/api/v1/couriers/**` | `lb://delivery-service` | delivery-service | 8087 |
| `/api/v1/inventory/**` | `lb://inventory-service` | inventory-service | 8085 |
| `/api/v1/notifications/**` | `lb://notification-service` | notification-service | 8088 |
| `/api/v1/preferences/**` | `lb://notification-service` | notification-service | 8088 |

### WebSocket (Direct Connection)

| Endpoint | Protocol | URL |
|----------|----------|-----|
| Kitchen KDS | STOMP over WebSocket / SockJS | `ws://localhost:8084/ws/kitchen` |

### Keycloak Auth

| Parameter | Value |
|-----------|-------|
| Authority | `http://localhost:9090/realms/pizzaflow` |
| Client ID | `pizzaflow-web` |
| Response type | `code` (Authorization Code + PKCE S256) |
| Redirect URI | `http://localhost:4200/callback` |
| Scopes | `openid profile email roles` |

### REST API Endpoints (by service)

#### Catalog Service (`/api/v1/catalog`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/menu/{restaurantId}` | Get full menu |
| GET | `/menu/{restaurantId}/category/{category}` | Menu items by category |
| GET | `/menu/{restaurantId}/featured` | Featured items |
| GET | `/menu/{restaurantId}/search?query=` | Search menu items |
| GET | `/items/{id}` | Get single menu item |
| POST | `/items` | Create menu item |
| PUT | `/items/{id}` | Update menu item |
| DELETE | `/items/{id}` | Delete menu item |

#### Order Service V1 (`/api/v1/orders`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create order |
| GET | `/{orderId}` | Get order by ID |
| GET | `/number/{orderNumber}` | Get by order number |
| GET | `/customer/{customerId}` | Get customer orders |
| PATCH | `/{orderId}/status?status=` | Update status |
| POST | `/{orderId}/cancel?reason=` | Cancel order |

#### Order Service V2 — Commands (`/api/v2/orders/commands`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/place` | Place order (event-sourced) |
| POST | `/{orderId}/confirm` | Confirm after payment |
| POST | `/{orderId}/prepare` | Start preparation |
| POST | `/{orderId}/ready` | Mark ready |
| POST | `/{orderId}/pickup` | Record courier pickup |
| POST | `/{orderId}/deliver` | Mark delivered |
| POST | `/{orderId}/complete` | Complete order |
| POST | `/{orderId}/cancel` | Cancel order |
| POST | `/{orderId}/items` | Add item |
| DELETE | `/{orderId}/items/{itemId}` | Remove item |

#### Order Service V2 — Queries (`/api/v2/orders/queries`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/{orderId}` | Get order by ID |
| GET | `/by-number/{orderNumber}` | Get by order number |
| GET | `/customer/{customerId}` | Customer orders |
| GET | `/customer/{customerId}/paged` | Paginated customer orders |
| GET | `/restaurant/{restaurantId}` | Restaurant orders (paged) |
| GET | `/status/{status}` | Orders by status |
| GET | `/kitchen/{restaurantId}` | Kitchen display orders |
| GET | `/ready/{restaurantId}` | Orders ready for pickup |
| GET | `/courier/{courierId}/active` | Active courier deliveries |
| GET | `/scheduled?from=&to=` | Scheduled orders |
| GET | `/restaurant/{restaurantId}/stats` | Order count stats |
| GET | `/search?q=` | Search orders |
| GET | `/stats/daily?from=` | Daily statistics |

#### Payment Service (`/api/v1/payments`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Process payment |
| GET | `/{transactionId}` | Get by transaction ID |
| GET | `/order/{orderId}` | Get by order ID |
| GET | `/customer/{customerId}` | Customer payments |
| POST | `/refund` | Process refund |
| GET | `/methods/{customerId}` | Saved payment methods |
| DELETE | `/methods/{paymentMethodId}` | Remove payment method |

#### Kitchen Service (`/api/v1/kitchen`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/queue/{restaurantId}` | Queue status |
| GET | `/orders/{orderId}` | Get kitchen order |
| PATCH | `/orders/{orderId}/status` | Update status |
| POST | `/orders/{orderId}/start?station=` | Start preparing |
| POST | `/orders/{orderId}/ready` | Mark ready |
| POST | `/orders/{orderId}/pickup` | Mark picked up |

#### Booking Service (`/api/v1/bookings`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/availability?restaurantId=&date=&partySize=` | Check availability |
| POST | `/` | Create booking |
| GET | `/{bookingId}` | Get booking |
| GET | `/number/{bookingNumber}` | Get by booking number |
| GET | `/customer/{customerId}` | Customer bookings |
| GET | `/restaurant/{restaurantId}/today` | Today's bookings |
| POST | `/{bookingId}/confirm` | Confirm |
| POST | `/{bookingId}/cancel?reason=` | Cancel |
| POST | `/{bookingId}/seat` | Seat guests |
| POST | `/{bookingId}/complete` | Complete |
| POST | `/{bookingId}/no-show` | Mark no-show |
| POST | `/{bookingId}/link-order` | Link pre-order |

#### Restaurant Service (`/api/v1/restaurants`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | List active restaurants |
| GET | `/{restaurantId}` | Get restaurant details |

#### Delivery Service (`/api/v1/deliveries`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create delivery |
| GET | `/{deliveryId}` | Get delivery |
| GET | `/order/{orderId}` | Get by order ID |
| GET | `/active` | All active deliveries |
| POST | `/{deliveryId}/assign?courierId=` | Assign courier |
| POST | `/{deliveryId}/pickup` | Mark picked up |
| POST | `/{deliveryId}/in-transit` | Mark in transit |
| POST | `/{deliveryId}/arrived` | Mark arrived |
| POST | `/{deliveryId}/complete?notes=` | Complete delivery |
| POST | `/{deliveryId}/fail?reason=` | Fail delivery |
| GET | `/{deliveryId}/track` | Track delivery |
| POST | `/{deliveryId}/location` | Update courier location |

#### Courier Service (`/api/v1/couriers`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/{courierId}` | Get courier |
| GET | `/user/{userId}` | Get by user ID |
| GET | `/available` | Available couriers |
| POST | `/{courierId}/online?latitude=&longitude=` | Go online |
| POST | `/{courierId}/offline` | Go offline |
| POST | `/{courierId}/status?status=` | Update status |
| POST | `/{courierId}/location` | Update GPS location |
| GET | `/{courierId}/deliveries` | Courier deliveries |

#### Inventory Service (`/api/v1/inventory`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/reservations` | Reserve ingredients |
| POST | `/reservations/{orderId}/consume` | Consume reserved |
| POST | `/reservations/{orderId}/release` | Release reservations |
| GET | `/stock/{restaurantId}` | Stock levels |
| GET | `/stock/{restaurantId}/low` | Low-stock items |
| POST | `/stock/adjust` | Adjust stock |
| GET | `/ingredients` | All ingredients |

#### Notification Service (`/api/v1/notifications`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Send notification |
| GET | `/users/{userId}` | User notification history (paged) |
| GET | `/users/{userId}/inbox` | Unread in-app notifications |
| GET | `/users/{userId}/unread-count` | Unread count |
| PUT | `/users/{userId}/notifications/{notificationId}/read` | Mark as read |
| PUT | `/users/{userId}/read-all` | Mark all read |
| POST | `/users/{userId}/archive` | Archive notifications |

#### Notification Preferences (`/api/v1/preferences`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/users/{userId}` | Get preferences |
| PUT | `/users/{userId}` | Update preferences |

### Domain Enums (TypeScript)

```typescript
enum OrderStatus { PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, COMPLETED, CANCELLED, FAILED }
enum OrderType { DELIVERY, PICKUP, DINE_IN, SCHEDULED, HYBRID }
enum PaymentStatus { PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED, PARTIALLY_REFUNDED }
enum PaymentMethodType { CREDIT_CARD, DEBIT_CARD, PAYPAL, APPLE_PAY, GOOGLE_PAY, CASH_ON_DELIVERY }
enum BookingStatus { PENDING, CONFIRMED, CANCELLED, NO_SHOW, SEATED, COMPLETED }
enum TableType { INDOOR, OUTDOOR, BAR, PRIVATE, VIP }
enum DeliveryStatus { PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, ARRIVED, DELIVERED, FAILED, CANCELLED }
enum CourierStatus { OFFLINE, AVAILABLE, ON_DELIVERY, BREAK }
enum KitchenOrderStatus { RECEIVED, PREPARING, READY, PICKED_UP, CANCELLED }
enum OrderPriority { LOW, NORMAL, HIGH, URGENT }
```

### Key DTO Structures (TypeScript Interfaces)

```typescript
// === Catalog ===
interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  imageUrl: string;
  category: "PIZZA" | "DRINK" | "SIDE" | "DESSERT";
  basePrice: number;
  available: boolean;
  featured: boolean;
  preparationTimeMinutes: number;
  allergens: string[];
  dietaryTags: string[];       // "VEGETARIAN", "VEGAN", "GLUTEN_FREE"
  nutritionalInfo: Record<string, any>;
  modifiers: Modifier[];
  recipe: Recipe;
  createdAt: string;
  updatedAt: string;
}

interface Modifier {
  id: string;
  name: string;
  type: "SIZE" | "TOPPING" | "EXTRA";
  options: ModifierOption[];
}

interface ModifierOption {
  id: string;
  name: string;
  priceAdjustment: number;
  available: boolean;
}

// === Order (V1) ===
interface CreateOrderRequest {
  customerId: number;
  restaurantId: number;
  orderType: OrderType;
  scheduledTime?: string;
  tableNumber?: string;
  reservationId?: number;
  deliveryAddress?: string;
  specialInstructions?: string;
  items: OrderItemRequest[];
}

interface OrderItemRequest {
  menuItemId: string;
  menuItemName: string;
  quantity: number;
  unitPrice: number;
  customizations?: string;
  specialInstructions?: string;
}

interface OrderResponse {
  id: number;
  orderNumber: string;
  customerId: number;
  restaurantId: number;
  orderType: OrderType;
  status: OrderStatus;
  scheduledTime?: string;
  tableNumber?: string;
  reservationId?: number;
  deliveryAddress?: string;
  specialInstructions?: string;
  items: OrderItemResponse[];
  subtotal: number;
  tax: number;
  deliveryFee: number;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  confirmedAt?: string;
  completedAt?: string;
}

// === Payment ===
interface PaymentRequest {
  orderId: number;
  customerId: number;
  amount: number;
  currency?: string;
  paymentMethodType: PaymentMethodType;
  cardNumber?: string;
  cardHolderName?: string;
  expiryDate?: string;
  cvv?: string;
}

interface PaymentResponse {
  transactionId: string;
  orderId: number;
  customerId: number;
  amount: number;
  currency: string;
  status: PaymentStatus;
  paymentMethodType: PaymentMethodType;
  gatewayTransactionId?: string;
  createdAt: string;
}

// === Kitchen ===
interface KitchenOrderDTO {
  id: string;
  orderId: number;
  orderNumber: string;
  restaurantId: number;
  customerId: number;
  orderType: string;
  status: KitchenOrderStatus;
  priority: OrderPriority;
  items: KitchenItemDTO[];
  estimatedPrepTimeMinutes: number;
  specialInstructions?: string;
  queuePosition: number;
  receivedAt: string;
  startedAt?: string;
  completedAt?: string;
  assignedStation?: string;
}

interface QueueStatusDTO {
  restaurantId: number;
  totalOrders: number;
  receivedCount: number;
  preparingCount: number;
  readyCount: number;
  averageWaitTimeMinutes: number;
  orders: KitchenOrderDTO[];
}
```

### Test Users

| Username | Password | Role | Use in Frontend |
|----------|----------|------|-----------------|
| customer | customer123 | CUSTOMER | Menu, cart, checkout, bookings |
| kitchen | kitchen123 | KITCHEN_STAFF | KDS board |
| courier | courier123 | COURIER | Delivery management |
| manager | manager123 | RESTAURANT_MANAGER | Dashboard, menu CRUD, analytics |
| admin | admin123 | SYSTEM_ADMIN | All views |
