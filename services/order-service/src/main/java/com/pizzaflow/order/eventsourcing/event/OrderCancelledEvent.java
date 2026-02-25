package com.pizzaflow.order.eventsourcing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event raised when an order is cancelled.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCancelledEvent extends OrderDomainEvent {

    /**
     * Time when the order was cancelled
     */
    private Instant cancelledAt;

    /**
     * Reason for cancellation
     */
    private String cancellationReason;

    /**
     * Who initiated the cancellation (CUSTOMER, RESTAURANT, SYSTEM)
     */
    private String cancelledBy;

    /**
     * Amount to be refunded, if any
     */
    private BigDecimal refundAmount;

    /**
     * Whether a refund has been processed
     */
    private Boolean refundProcessed;

    @Override
    public String getEventType() {
        return "ORDER_CANCELLED";
    }
}
