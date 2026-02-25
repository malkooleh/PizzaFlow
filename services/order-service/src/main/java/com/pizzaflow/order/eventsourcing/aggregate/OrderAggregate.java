package com.pizzaflow.order.eventsourcing.aggregate;

import com.pizzaflow.order.eventsourcing.event.*;
import com.pizzaflow.order.model.enums.OrderStatus;
import com.pizzaflow.order.model.enums.OrderType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Order Aggregate - The core domain entity for event sourcing.
 * 
 * This aggregate:
 * - Maintains the current state of an order
 * - Processes commands and emits events
 * - Can be reconstructed from a stream of events
 * - Enforces business invariants
 */
@Slf4j
@Getter
public class OrderAggregate {

    // Aggregate identity
    private UUID id;
    private Long version = 0L;

    // Order information
    private String orderNumber;
    private UUID customerId;
    private UUID restaurantId;
    private OrderType orderType;
    private OrderStatus status;

    // Pricing
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Delivery address
    private String deliveryAddressStreet;
    private String deliveryAddressCity;
    private String deliveryAddressPostalCode;
    private BigDecimal deliveryAddressLatitude;
    private BigDecimal deliveryAddressLongitude;

    // Scheduling
    private LocalDateTime scheduledTime;
    private String tableNumber;
    private UUID reservationId;

    // Timestamps
    private Instant createdAt;
    private Instant confirmedAt;
    private Instant preparingAt;
    private Instant readyAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private Instant completedAt;
    private Instant cancelledAt;

    // Cancellation
    private String cancellationReason;
    private String cancelledBy;

    // Delivery
    private UUID courierId;
    private String courierName;
    private Instant estimatedDeliveryTime;

    // Order items
    private final Map<UUID, OrderItemState> items = new HashMap<>();

    // Uncommitted events (events generated but not yet persisted)
    private final List<OrderDomainEvent> uncommittedEvents = new ArrayList<>();

    /**
     * Default constructor for creating a new aggregate
     */
    public OrderAggregate() {
    }

    /**
     * Constructor for reconstituting aggregate from events
     */
    public OrderAggregate(UUID id) {
        this.id = id;
    }

    // ===========================================
    // COMMAND HANDLERS (Business Operations)
    // ===========================================

    /**
     * Place a new order
     */
    public void placeOrder(PlaceOrderCommand command) {
        if (this.status != null) {
            throw new IllegalStateException("Order already exists");
        }

        validatePlaceOrderCommand(command);

        List<OrderPlacedEvent.OrderItemData> itemDataList = command.getItems().stream()
                .map(item -> OrderPlacedEvent.OrderItemData.builder()
                        .itemId(UUID.randomUUID())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .specialInstructions(item.getSpecialInstructions())
                        .build())
                .collect(Collectors.toList());

        BigDecimal subtotal = itemDataList.stream()
                .map(OrderPlacedEvent.OrderItemData::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.08"));
        BigDecimal deliveryFee = calculateDeliveryFee(command.getOrderType());
        BigDecimal total = subtotal.add(tax).add(deliveryFee);

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .aggregateId(this.id)
                .orderNumber(generateOrderNumber())
                .customerId(command.getCustomerId())
                .restaurantId(command.getRestaurantId())
                .orderType(command.getOrderType().name())
                .scheduledTime(command.getScheduledTime())
                .tableNumber(command.getTableNumber())
                .reservationId(command.getReservationId())
                .deliveryAddressStreet(command.getDeliveryAddressStreet())
                .deliveryAddressCity(command.getDeliveryAddressCity())
                .deliveryAddressPostalCode(command.getDeliveryAddressPostalCode())
                .deliveryAddressLatitude(command.getDeliveryAddressLatitude())
                .deliveryAddressLongitude(command.getDeliveryAddressLongitude())
                .subtotal(subtotal)
                .tax(tax)
                .deliveryFee(deliveryFee)
                .totalAmount(total)
                .items(itemDataList)
                .triggeredBy(command.getTriggeredBy())
                .correlationId(command.getCorrelationId())
                .build();

        applyEvent(event);
    }

    /**
     * Confirm the order (after payment)
     */
    public void confirmOrder(String paymentReference, Integer estimatedPrepTimeMinutes, String triggeredBy) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only confirm orders in PENDING status. Current: " + this.status);
        }

        OrderConfirmedEvent event = OrderConfirmedEvent.builder()
                .aggregateId(this.id)
                .paymentReference(paymentReference)
                .confirmedAt(Instant.now())
                .estimatedPrepTimeMinutes(estimatedPrepTimeMinutes)
                .triggeredBy(triggeredBy)
                .build();

        applyEvent(event);
    }

    /**
     * Start preparing the order
     */
    public void startPreparing(String kitchenStation, String assignedStaff, Instant estimatedCompletion,
            String triggeredBy) {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Can only start preparing orders in CONFIRMED status. Current: " + this.status);
        }

        OrderPreparingEvent event = OrderPreparingEvent.builder()
                .aggregateId(this.id)
                .preparationStartedAt(Instant.now())
                .kitchenStation(kitchenStation)
                .assignedStaff(assignedStaff)
                .estimatedCompletionTime(estimatedCompletion)
                .triggeredBy(triggeredBy)
                .build();

        applyEvent(event);
    }

    /**
     * Mark order as ready
     */
    public void markReady(String pickupLocation, String packagingNotes, String triggeredBy) {
        if (this.status != OrderStatus.PREPARING) {
            throw new IllegalStateException(
                    "Can only mark orders ready when in PREPARING status. Current: " + this.status);
        }

        OrderReadyEvent event = OrderReadyEvent.builder()
                .aggregateId(this.id)
                .readyAt(Instant.now())
                .pickupLocation(pickupLocation)
                .packagingNotes(packagingNotes)
                .triggeredBy(triggeredBy)
                .build();

        applyEvent(event);
    }

    /**
     * Record order pickup by courier
     */
    public void pickUp(UUID courierId, String courierName, Instant estimatedDelivery, String triggeredBy) {
        if (this.status != OrderStatus.READY) {
            throw new IllegalStateException("Can only pick up orders in READY status. Current: " + this.status);
        }

        if (this.orderType != OrderType.DELIVERY && this.orderType != OrderType.SCHEDULED) {
            throw new IllegalStateException("Only delivery orders can be picked up by courier");
        }

        OrderPickedUpEvent event = OrderPickedUpEvent.builder()
                .aggregateId(this.id)
                .pickedUpAt(Instant.now())
                .courierId(courierId)
                .courierName(courierName)
                .estimatedDeliveryTime(estimatedDelivery)
                .triggeredBy(triggeredBy)
                .build();

        applyEvent(event);
    }

    /**
     * Mark order as delivered
     */
    public void deliver(String receivedBy, String deliveryConfirmation, String notes, String triggeredBy) {
        if (this.status != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException(
                    "Can only deliver orders in OUT_FOR_DELIVERY status. Current: " + this.status);
        }

        OrderDeliveredEvent event = OrderDeliveredEvent.builder()
                .aggregateId(this.id)
                .deliveredAt(Instant.now())
                .courierId(this.courierId)
                .receivedBy(receivedBy)
                .deliveryConfirmation(deliveryConfirmation)
                .deliveryNotes(notes)
                .triggeredBy(triggeredBy)
                .build();

        applyEvent(event);
    }

    /**
     * Complete the order (for dine-in or pickup)
     */
    public void complete(Integer rating, String feedback, String triggeredBy) {
        if (this.status != OrderStatus.READY && this.status != OrderStatus.DELIVERED) {
            throw new IllegalStateException(
                    "Can only complete orders in READY or DELIVERED status. Current: " + this.status);
        }

        OrderCompletedEvent event = OrderCompletedEvent.builder()
                .aggregateId(this.id)
                .completedAt(Instant.now())
                .customerRating(rating)
                .customerFeedback(feedback)
                .triggeredBy(triggeredBy)
                .build();

        applyEvent(event);
    }

    /**
     * Cancel the order
     */
    public void cancel(String reason, String cancelledBy, BigDecimal refundAmount, String triggeredBy) {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.COMPLETED
                || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + this.status);
        }

        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .aggregateId(this.id)
                .cancelledAt(Instant.now())
                .cancellationReason(reason)
                .cancelledBy(cancelledBy)
                .refundAmount(refundAmount)
                .refundProcessed(false)
                .triggeredBy(triggeredBy)
                .build();

        applyEvent(event);
    }

    /**
     * Add an item to the order (only before preparation starts)
     */
    public void addItem(String itemId, String itemName, Integer quantity, BigDecimal unitPrice, String instructions,
            String triggeredBy) {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot modify order in status: " + this.status);
        }

        BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal newSubtotal = this.subtotal.add(itemTotal);
        BigDecimal newTax = newSubtotal.multiply(new BigDecimal("0.08"));
        BigDecimal newTotal = newSubtotal.add(newTax).add(this.deliveryFee);

        OrderItemAddedEvent event = OrderItemAddedEvent.builder()
                .aggregateId(this.id)
                .orderItemId(UUID.randomUUID())
                .itemId(itemId)
                .itemName(itemName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(itemTotal)
                .specialInstructions(instructions)
                .newSubtotal(newSubtotal)
                .newTotal(newTotal)
                .triggeredBy(triggeredBy)
                .build();

        applyEvent(event);
    }

    /**
     * Remove an item from the order
     */
    public void removeItem(UUID orderItemId, String reason, String triggeredBy) {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot modify order in status: " + this.status);
        }

        OrderItemState item = this.items.get(orderItemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found: " + orderItemId);
        }

        BigDecimal newSubtotal = this.subtotal.subtract(item.getTotalPrice());
        BigDecimal newTax = newSubtotal.multiply(new BigDecimal("0.08"));
        BigDecimal newTotal = newSubtotal.add(newTax).add(this.deliveryFee);

        OrderItemRemovedEvent event = OrderItemRemovedEvent.builder()
                .aggregateId(this.id)
                .orderItemId(orderItemId)
                .removalReason(reason)
                .newSubtotal(newSubtotal)
                .newTotal(newTotal)
                .triggeredBy(triggeredBy)
                .build();

        applyEvent(event);
    }

    // ===========================================
    // EVENT APPLICATION (State Updates)
    // ===========================================

    /**
     * Apply an event and add to uncommitted events
     */
    private void applyEvent(OrderDomainEvent event) {
        event.setVersion(this.version + 1);
        event.initializeEvent();
        apply(event);
        uncommittedEvents.add(event);
    }

    /**
     * Apply event to update state (used for both new events and replay)
     */
    public void apply(OrderDomainEvent event) {
        switch (event) {
            case OrderPlacedEvent e -> applyOrderPlaced(e);
            case OrderConfirmedEvent e -> applyOrderConfirmed(e);
            case OrderPreparingEvent e -> applyOrderPreparing(e);
            case OrderReadyEvent e -> applyOrderReady(e);
            case OrderPickedUpEvent e -> applyOrderPickedUp(e);
            case OrderDeliveredEvent e -> applyOrderDelivered(e);
            case OrderCompletedEvent e -> applyOrderCompleted(e);
            case OrderCancelledEvent e -> applyOrderCancelled(e);
            case OrderItemAddedEvent e -> applyOrderItemAdded(e);
            case OrderItemRemovedEvent e -> applyOrderItemRemoved(e);
            default -> log.warn("Unknown event type: {}", event.getClass().getSimpleName());
        }
        this.version = event.getVersion();
    }

    private void applyOrderPlaced(OrderPlacedEvent event) {
        this.id = event.getAggregateId();
        this.orderNumber = event.getOrderNumber();
        this.customerId = event.getCustomerId();
        this.restaurantId = event.getRestaurantId();
        this.orderType = OrderType.valueOf(event.getOrderType());
        this.status = OrderStatus.PENDING;
        this.scheduledTime = event.getScheduledTime();
        this.tableNumber = event.getTableNumber();
        this.reservationId = event.getReservationId();
        this.deliveryAddressStreet = event.getDeliveryAddressStreet();
        this.deliveryAddressCity = event.getDeliveryAddressCity();
        this.deliveryAddressPostalCode = event.getDeliveryAddressPostalCode();
        this.deliveryAddressLatitude = event.getDeliveryAddressLatitude();
        this.deliveryAddressLongitude = event.getDeliveryAddressLongitude();
        this.subtotal = event.getSubtotal();
        this.tax = event.getTax();
        this.deliveryFee = event.getDeliveryFee();
        this.totalAmount = event.getTotalAmount();
        this.createdAt = event.getTimestamp();

        if (event.getItems() != null) {
            for (OrderPlacedEvent.OrderItemData itemData : event.getItems()) {
                OrderItemState itemState = new OrderItemState(
                        itemData.getItemId(),
                        itemData.getItemId().toString(),
                        itemData.getItemName(),
                        itemData.getQuantity(),
                        itemData.getUnitPrice(),
                        itemData.getTotalPrice(),
                        itemData.getSpecialInstructions());
                this.items.put(itemData.getItemId(), itemState);
            }
        }
    }

    private void applyOrderConfirmed(OrderConfirmedEvent event) {
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = event.getConfirmedAt();
    }

    private void applyOrderPreparing(OrderPreparingEvent event) {
        this.status = OrderStatus.PREPARING;
        this.preparingAt = event.getPreparationStartedAt();
    }

    private void applyOrderReady(OrderReadyEvent event) {
        this.status = OrderStatus.READY;
        this.readyAt = event.getReadyAt();
    }

    private void applyOrderPickedUp(OrderPickedUpEvent event) {
        this.status = OrderStatus.OUT_FOR_DELIVERY;
        this.pickedUpAt = event.getPickedUpAt();
        this.courierId = event.getCourierId();
        this.courierName = event.getCourierName();
        this.estimatedDeliveryTime = event.getEstimatedDeliveryTime();
    }

    private void applyOrderDelivered(OrderDeliveredEvent event) {
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = event.getDeliveredAt();
    }

    private void applyOrderCompleted(OrderCompletedEvent event) {
        this.status = OrderStatus.COMPLETED;
        this.completedAt = event.getCompletedAt();
    }

    private void applyOrderCancelled(OrderCancelledEvent event) {
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = event.getCancelledAt();
        this.cancellationReason = event.getCancellationReason();
        this.cancelledBy = event.getCancelledBy();
    }

    private void applyOrderItemAdded(OrderItemAddedEvent event) {
        OrderItemState itemState = new OrderItemState(
                event.getOrderItemId(),
                event.getItemId(),
                event.getItemName(),
                event.getQuantity(),
                event.getUnitPrice(),
                event.getTotalPrice(),
                event.getSpecialInstructions());
        this.items.put(event.getOrderItemId(), itemState);
        this.subtotal = event.getNewSubtotal();
        this.tax = this.subtotal.multiply(new BigDecimal("0.08"));
        this.totalAmount = event.getNewTotal();
    }

    private void applyOrderItemRemoved(OrderItemRemovedEvent event) {
        this.items.remove(event.getOrderItemId());
        this.subtotal = event.getNewSubtotal();
        this.tax = this.subtotal.multiply(new BigDecimal("0.08"));
        this.totalAmount = event.getNewTotal();
    }

    // ===========================================
    // HELPER METHODS
    // ===========================================

    /**
     * Get and clear uncommitted events
     */
    public List<OrderDomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }

    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    private void validatePlaceOrderCommand(PlaceOrderCommand command) {
        if (command.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (command.getRestaurantId() == null) {
            throw new IllegalArgumentException("Restaurant ID is required");
        }
        if (command.getOrderType() == null) {
            throw new IllegalArgumentException("Order type is required");
        }
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }
    }

    private BigDecimal calculateDeliveryFee(OrderType orderType) {
        return switch (orderType) {
            case DELIVERY, SCHEDULED -> new BigDecimal("5.00");
            default -> BigDecimal.ZERO;
        };
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Inner class to hold order item state
     */
    @Getter
    public static class OrderItemState {
        private final UUID id;
        private final String itemId;
        private final String itemName;
        private final Integer quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal totalPrice;
        private final String specialInstructions;

        public OrderItemState(UUID id, String itemId, String itemName, Integer quantity,
                BigDecimal unitPrice, BigDecimal totalPrice, String specialInstructions) {
            this.id = id;
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
            this.specialInstructions = specialInstructions;
        }
    }
}
