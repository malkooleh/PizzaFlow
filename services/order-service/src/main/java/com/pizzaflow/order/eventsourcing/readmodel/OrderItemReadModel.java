package com.pizzaflow.order.eventsourcing.readmodel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Read model entity for order items.
 */
@Entity
@Table(name = "order_items_read_model", indexes = {
        @Index(name = "idx_order_items_read_order_id", columnList = "order_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemReadModel {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderReadModel order;

    @Column(name = "item_id", nullable = false, length = 100)
    private String itemId;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;
}
