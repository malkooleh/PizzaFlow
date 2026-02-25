package com.pizzaflow.order.eventsourcing.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all order domain events in the event sourcing system.
 * Uses Jackson polymorphic type handling for proper
 * serialization/deserialization.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderPlacedEvent.class, name = "ORDER_PLACED"),
        @JsonSubTypes.Type(value = OrderConfirmedEvent.class, name = "ORDER_CONFIRMED"),
        @JsonSubTypes.Type(value = OrderPreparingEvent.class, name = "ORDER_PREPARING"),
        @JsonSubTypes.Type(value = OrderReadyEvent.class, name = "ORDER_READY"),
        @JsonSubTypes.Type(value = OrderPickedUpEvent.class, name = "ORDER_PICKED_UP"),
        @JsonSubTypes.Type(value = OrderDeliveredEvent.class, name = "ORDER_DELIVERED"),
        @JsonSubTypes.Type(value = OrderCompletedEvent.class, name = "ORDER_COMPLETED"),
        @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = "ORDER_CANCELLED"),
        @JsonSubTypes.Type(value = OrderItemAddedEvent.class, name = "ORDER_ITEM_ADDED"),
        @JsonSubTypes.Type(value = OrderItemRemovedEvent.class, name = "ORDER_ITEM_REMOVED")
})
public abstract class OrderDomainEvent {

    /**
     * Unique identifier for this event
     */
    private UUID eventId;

    /**
     * Aggregate identifier (order ID)
     */
    private UUID aggregateId;

    /**
     * Version number for optimistic concurrency control
     */
    private Long version;

    /**
     * Timestamp when the event occurred
     */
    private Instant timestamp;

    /**
     * User or service that triggered this event
     */
    private String triggeredBy;

    /**
     * Correlation ID for distributed tracing
     */
    private String correlationId;

    /**
     * Returns the event type name for persistence and serialization
     */
    public abstract String getEventType();

    /**
     * Create event with auto-generated eventId and current timestamp
     */
    public void initializeEvent() {
        if (this.eventId == null) {
            this.eventId = UUID.randomUUID();
        }
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
