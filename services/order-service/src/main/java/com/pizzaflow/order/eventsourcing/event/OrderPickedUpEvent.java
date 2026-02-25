package com.pizzaflow.order.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Event raised when a delivery driver picks up the order.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderPickedUpEvent extends OrderDomainEvent {

    /**
     * Time when the order was picked up
     */
    private Instant pickedUpAt;

    /**
     * ID of the courier/driver
     */
    private UUID courierId;

    /**
     * Name of the courier/driver
     */
    private String courierName;

    /**
     * Estimated delivery time
     */
    private Instant estimatedDeliveryTime;

    @Override
    public String getEventType() {
        return "ORDER_PICKED_UP";
    }
}
