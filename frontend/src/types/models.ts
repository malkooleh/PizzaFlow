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

export interface AvailabilitySlot {
  time: string; // "HH:mm"
  available: boolean;
  remainingCapacity: number;
}

export interface BookingResponse {
  id: string; // UUID
  bookingNumber: string;
  restaurantId: string;
  customerId: string;
  guestName: string;
  guestEmail: string;
  guestPhone: string;
  bookingDate: string;
  bookingTime: string;
  partySize: number;
  tableType: TableType;
  status: BookingStatus;
  specialRequests?: string;
  tableNumber?: string;
  linkedOrderId?: number;
  createdAt: string;
  confirmedAt?: string;
  cancelledAt?: string;
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
