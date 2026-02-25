package com.pizzaflow.order.eventsourcing.readmodel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Read model entity for orders - optimized for queries (CQRS read side).
 * This is a denormalized view updated asynchronously by event projections.
 */
@Entity
@Table(name = "order_read_model", indexes = {
        @Index(name = "idx_order_read_customer_id", columnList = "customer_id"),
        @Index(name = "idx_order_read_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_order_read_status", columnList = "status"),
        @Index(name = "idx_order_read_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadModel {

    @Id
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    // Pricing
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax", nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Delivery address
    @Column(name = "delivery_address_street", length = 255)
    private String deliveryAddressStreet;

    @Column(name = "delivery_address_city", length = 100)
    private String deliveryAddressCity;

    @Column(name = "delivery_address_postal_code", length = 20)
    private String deliveryAddressPostalCode;

    @Column(name = "delivery_address_latitude", precision = 10, scale = 8)
    private BigDecimal deliveryAddressLatitude;

    @Column(name = "delivery_address_longitude", precision = 11, scale = 8)
    private BigDecimal deliveryAddressLongitude;

    // Scheduling
    @Column(name = "scheduled_time")
    private Instant scheduledTime;

    @Column(name = "table_number", length = 20)
    private String tableNumber;

    @Column(name = "booking_id")
    private UUID bookingId;

    // Timestamps
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "preparing_at")
    private Instant preparingAt;

    @Column(name = "ready_at")
    private Instant readyAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    // Cancellation
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_by", length = 50)
    private String cancelledBy;

    // Delivery tracking
    @Column(name = "courier_id")
    private UUID courierId;

    @Column(name = "courier_name", length = 100)
    private String courierName;

    @Column(name = "estimated_delivery_time")
    private Instant estimatedDeliveryTime;

    // Version for optimistic locking
    @Column(name = "version", nullable = false)
    @Version
    private Long version;

    @Column(name = "last_event_id")
    private UUID lastEventId;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    // Order items (lazy loaded for performance)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItemReadModel> items = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = Instant.now();
    }

    public void addItem(OrderItemReadModel item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItemReadModel item) {
        items.remove(item);
        item.setOrder(null);
    }
}
