package com.pizzaflow.order.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Event raised when an order is successfully delivered.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderDeliveredEvent extends OrderDomainEvent {

    /**
     * Time when the order was delivered
     */
    private Instant deliveredAt;

    /**
     * ID of the courier who made the delivery
     */
    private UUID courierId;

    /**
     * Name of the person who received the order
     */
    private String receivedBy;

    /**
     * Digital signature or confirmation code
     */
    private String deliveryConfirmation;

    /**
     * Additional delivery notes
     */
    private String deliveryNotes;

    @Override
    public String getEventType() {
        return "ORDER_DELIVERED";
    }
}
