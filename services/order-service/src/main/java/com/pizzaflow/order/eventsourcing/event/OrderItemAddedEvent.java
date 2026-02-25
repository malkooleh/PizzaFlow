package com.pizzaflow.order.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event raised when an item is added to an existing order.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderItemAddedEvent extends OrderDomainEvent {

    /**
     * Unique identifier for this order item
     */
    private UUID orderItemId;

    /**
     * Menu item identifier
     */
    private String itemId;

    /**
     * Name of the menu item
     */
    private String itemName;

    /**
     * Quantity added
     */
    private Integer quantity;

    /**
     * Unit price of the item
     */
    private BigDecimal unitPrice;

    /**
     * Total price for this item (quantity * unitPrice)
     */
    private BigDecimal totalPrice;

    /**
     * Special instructions for this item
     */
    private String specialInstructions;

    /**
     * Updated order subtotal after adding this item
     */
    private BigDecimal newSubtotal;

    /**
     * Updated order total after adding this item
     */
    private BigDecimal newTotal;

    @Override
    public String getEventType() {
        return "ORDER_ITEM_ADDED";
    }
}
