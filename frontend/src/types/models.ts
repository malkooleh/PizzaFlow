import type {
  OrderStatus,
  OrderType,
  PaymentStatus,
  PaymentMethodType,
  MenuCategory,
  KitchenOrderStatus,
  OrderPriority,
  BookingStatus,
  TableType,
  DeliveryStatus,
  CourierStatus,
  AlertSeverity,
  ServiceStatus,
  NotificationType,
} from "./enums";

// ============================================================
// Restaurant / Catalog
// ============================================================

export interface Restaurant {
  id: string; // UUID
  name: string;
  address: string;
  phone: string;
  openingTime: string; // "HH:mm"
  closingTime: string;
  isActive: boolean;
  imageUrl?: string;
}

export interface MenuItemModifier {
  modifierId: string;
  name: string;
  additionalPrice: number;
}

export interface ModifierGroup {
  name: string;
  required: boolean;
  maxSelections: number;
  options: MenuItemModifier[];
}

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  price: number;
  category: MenuCategory;
  imageUrl?: string;
  ingredients: string[];
  allergens: string[];
  calories?: number;
  isVegetarian: boolean;
  isVegan: boolean;
  isGlutenFree: boolean;
  isAvailable: boolean;
  isFeatured: boolean;
  preparationTimeMinutes: number;
  modifierGroups: ModifierGroup[];
}

// ============================================================
// Order
// ============================================================

export interface Address {
  street: string;
  city: string;
  district?: string;
  state?: string;
  zipCode: string;
  country: string;
  additionalInfo?: string;
}

export interface OrderItemResponse {
  id: number;
  menuItemId: string;
  menuItemName: string;
  quantity: number;
  unitPrice: number;
  customizations?: string;
  specialInstructions?: string;
}

export interface OrderResponse {
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

// ============================================================
// Payment
// ============================================================

export interface PaymentResponse {
  transactionId: string;
  orderId: number;
  customerId: number;
  amount: number;
  currency: string;
  status: PaymentStatus;
  paymentMethodType: PaymentMethodType;
  gatewayTransactionId?: string;
  failureReason?: string;
  createdAt: string;
  updatedAt?: string;
}

// ============================================================
// Kitchen
// ============================================================

export interface KitchenItemDTO {
  menuItemId: string;
  menuItemName: string;
  quantity: number;
  modifications?: string;
  specialInstructions?: string;
}

export interface KitchenOrderDTO {
  id: string; // UUID
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

export interface QueueStatusDTO {
  restaurantId: number;
  totalOrders: number;
  receivedCount: number;
  preparingCount: number;
  readyCount: number;
  averageWaitTimeMinutes: number;
  orders: KitchenOrderDTO[];
}

// ============================================================
// Booking
// ============================================================

export interface AvailableTableInfo {
  tableId: string;
  tableName: string;
  capacity: number;
  tableType: TableType;
}

export interface TimeSlot {
  startTime: string; // ISO datetime e.g. "2026-03-15T19:00:00"
  endTime: string;
  availableCapacity: number;
  availableTables: AvailableTableInfo[];
}

export interface AvailabilityResponse {
  restaurantId: string;
  restaurantName: string;
  date: string; // "YYYY-MM-DD"
  requestedPartySize: number;
  availableSlots: TimeSlot[];
  totalCapacity: number;
  fullyBooked: boolean;
}

export interface BookingRestaurant {
  id: string; // UUID
  name: string;
  address: string;
  openingTime: string; // "HH:mm:ss"
  closingTime: string;
  maxPartySize: number;
  bookingSlotDurationMinutes: number;
  isActive: boolean;
}

export interface BookingResponse {
  id: string; // UUID
  bookingNumber: string;
  customerId: string; // UUID
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  restaurantId: string; // UUID
  restaurantName: string;
  tableId: string | null; // UUID
  tableName: string | null;
  tableType: TableType | null;
  reservationTime: string; // ISO datetime
  endTime: string; // ISO datetime
  partySize: number;
  status: BookingStatus;
  specialRequests: string | null;
  preOrderId: string | null; // UUID
  createdAt: string;
  updatedAt: string;
}

// ============================================================
// Delivery
// ============================================================

export interface DeliveryLocation {
  latitude: number;
  longitude: number;
  address?: string;
}

export interface DeliveryResponse {
  id: string; // UUID
  deliveryNumber: string;
  orderId: number;
  orderNumber: string;
  restaurantId: string;
  customerId: string;
  courierId?: string;
  status: DeliveryStatus;
  pickupAddress: string;
  deliveryAddress: string;
  estimatedDeliveryTime?: string;
  actualDeliveryTime?: string;
  currentLocation?: DeliveryLocation;
  distanceKm?: number;
  createdAt: string;
  updatedAt: string;
}

// ============================================================
// Notification
// ============================================================

export interface NotificationResponse {
  id: string; // UUID
  recipientId: string;
  type: NotificationType;
  title: string;
  message: string;
  isRead: boolean;
  metadata?: Record<string, string>;
  createdAt: string;
  readAt?: string;
}

// ============================================================
// Courier
// ============================================================
export interface CourierResponse {
  id: string; // UUID
  userId: string;
  name: string;
  phone: string;
  email: string;
  vehicleType: string;
  licenseNumber?: string;
  status: CourierStatus;
  currentLocation?: DeliveryLocation;
  isOnline: boolean;
  createdAt: string;
  updatedAt: string;
}

// ============================================================
// Inventory
// ============================================================
export interface InventoryStockLevel {
  ingredientId: string;
  ingredientName: string;
  restaurantId: string;
  currentStock: number;
  minStockLevel: number;
  unit: string;
  lastUpdated: string;
}

export interface StockAdjustRequest {
  restaurantId: string;
  ingredientId: string;
  quantity: number;
  reason?: string;
}

// ============================================================
// Manager / Analytics
// ============================================================
export interface OrderDailyStats {
  date: string;
  orderCount: number;
  revenue: number;
  averageOrderValue: number;
}

// ============================================================
// Admin
// ============================================================
export interface ServiceHealthStatus {
  serviceName: string;
  status: ServiceStatus;
  uptimePercent: number;
  p95LatencyMs: number;
  errorRatePercent: number;
  lastHeartbeat: string;
  instanceCount: number;
}

export interface BusinessKPI {
  totalOrdersToday: number;
  revenueToday: number;
  activeDeliveries: number;
  pendingBookings: number;
  averageOrderValue: number;
  paymentSuccessRate: number;
}

export interface AuditEntry {
  id: string;
  actorId: string;
  actorRole: string;
  action: string;
  resourceType: string;
  resourceId: string;
  timestamp: string;
  correlationId?: string;
  details?: Record<string, string>;
}

export interface AlertItem {
  id: string;
  title: string;
  description: string;
  severity: AlertSeverity;
  serviceName: string;
  isAcknowledged: boolean;
  acknowledgedBy?: string;
  acknowledgedAt?: string;
  createdAt: string;
}
