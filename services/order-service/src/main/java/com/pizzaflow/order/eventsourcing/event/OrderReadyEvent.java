package com.pizzaflow.order.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event raised when an order is ready for pickup or delivery.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderReadyEvent extends OrderDomainEvent {

    /**
     * Time when the order became ready
     */
    private Instant readyAt;

    /**
     * Pickup location within the restaurant
     */
    private String pickupLocation;

    /**
     * Packaging details
     */
    private String packagingNotes;

    @Override
    public String getEventType() {
        return "ORDER_READY";
    }
}
