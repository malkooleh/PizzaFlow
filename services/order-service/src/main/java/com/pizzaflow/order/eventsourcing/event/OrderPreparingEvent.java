package com.pizzaflow.order.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event raised when the kitchen starts preparing an order.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderPreparingEvent extends OrderDomainEvent {

    /**
     * Time when preparation started
     */
    private Instant preparationStartedAt;

    /**
     * Kitchen station assigned to this order
     */
    private String kitchenStation;

    /**
     * Staff member assigned to prepare this order
     */
    private String assignedStaff;

    /**
     * Updated estimated completion time
     */
    private Instant estimatedCompletionTime;

    @Override
    public String getEventType() {
        return "ORDER_PREPARING";
    }
}
