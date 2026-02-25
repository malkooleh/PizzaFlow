package com.pizzaflow.order.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event raised when an item is removed from an existing order.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderItemRemovedEvent extends OrderDomainEvent {

    /**
     * Unique identifier of the order item being removed
     */
    private UUID orderItemId;

    /**
     * Reason for removal
     */
    private String removalReason;

    /**
     * Updated order subtotal after removing this item
     */
    private BigDecimal newSubtotal;

    /**
     * Updated order total after removing this item
     */
    private BigDecimal newTotal;

    @Override
    public String getEventType() {
        return "ORDER_ITEM_REMOVED";
    }
}
