package com.pizzaflow.order.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event raised when an order is fully completed (for dine-in or pickup orders).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCompletedEvent extends OrderDomainEvent {

    /**
     * Time when the order was completed
     */
    private Instant completedAt;

    /**
     * Customer rating (1-5) if provided
     */
    private Integer customerRating;

    /**
     * Customer feedback if provided
     */
    private String customerFeedback;

    @Override
    public String getEventType() {
        return "ORDER_COMPLETED";
    }
}
