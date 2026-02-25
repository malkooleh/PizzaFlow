package com.pizzaflow.order.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event raised when an order is confirmed (typically after payment
 * verification).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderConfirmedEvent extends OrderDomainEvent {

    /**
     * Reference to payment transaction if applicable
     */
    private String paymentReference;

    /**
     * Time when the order was confirmed
     */
    private Instant confirmedAt;

    /**
     * Estimated preparation time in minutes
     */
    private Integer estimatedPrepTimeMinutes;

    @Override
    public String getEventType() {
        return "ORDER_CONFIRMED";
    }
}
