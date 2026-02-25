package com.pizzaflow.order.eventsourcing.aggregate;

import com.pizzaflow.order.eventsourcing.event.OrderDomainEvent;
import com.pizzaflow.order.eventsourcing.store.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for loading and saving Order aggregates using event sourcing.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderAggregateRepository {

    private final EventStore eventStore;

    /**
     * Load an aggregate by reconstituting from events.
     * 
     * @param aggregateId The aggregate ID
     * @return Optional containing the aggregate if it exists
     */
    public Optional<OrderAggregate> findById(UUID aggregateId) {
        if (!eventStore.aggregateExists(aggregateId)) {
            return Optional.empty();
        }

        // Try to load from snapshot first
        Optional<EventStore.SnapshotWithVersion<OrderAggregateSnapshot>> snapshot = eventStore
                .loadLatestSnapshot(aggregateId, OrderAggregateSnapshot.class);

        OrderAggregate aggregate;
        Long fromVersion;

        if (snapshot.isPresent()) {
            log.debug("Loading aggregate {} from snapshot at version {}",
                    aggregateId, snapshot.get().version());
            aggregate = snapshot.get().state().toAggregate();
            fromVersion = snapshot.get().version();
        } else {
            aggregate = new OrderAggregate(aggregateId);
            fromVersion = 0L;
        }

        // Load and apply events after snapshot
        List<OrderDomainEvent> events = fromVersion == 0L
                ? eventStore.loadEvents(aggregateId)
                : eventStore.loadEventsFromVersion(aggregateId, fromVersion);

        for (OrderDomainEvent event : events) {
            aggregate.apply(event);
        }

        log.debug("Loaded aggregate {} with version {}", aggregateId, aggregate.getVersion());
        return Optional.of(aggregate);
    }

    /**
     * Load an aggregate, throwing exception if not found.
     */
    public OrderAggregate getById(UUID aggregateId) {
        return findById(aggregateId)
                .orElseThrow(() -> new AggregateNotFoundException("Order not found: " + aggregateId));
    }

    /**
     * Save an aggregate by persisting its uncommitted events.
     * 
     * @param aggregate The aggregate to save
     */
    public void save(OrderAggregate aggregate) {
        List<OrderDomainEvent> events = aggregate.getUncommittedEvents();

        if (events.isEmpty()) {
            log.debug("No events to save for aggregate {}", aggregate.getId());
            return;
        }

        // Expected version is current version minus number of uncommitted events
        Long expectedVersion = aggregate.getVersion() - events.size();

        eventStore.appendEvents(aggregate.getId(), events, expectedVersion);

        // Create snapshot if threshold reached
        if (eventStore.shouldCreateSnapshot(aggregate.getId())) {
            log.debug("Creating snapshot for aggregate {} at version {}",
                    aggregate.getId(), aggregate.getVersion());
            OrderAggregateSnapshot snapshot = OrderAggregateSnapshot.fromAggregate(aggregate);
            eventStore.saveSnapshot(aggregate.getId(), snapshot, aggregate.getVersion());
        }

        aggregate.clearUncommittedEvents();
        log.debug("Saved {} events for aggregate {}", events.size(), aggregate.getId());
    }

    /**
     * Create and save a new aggregate.
     */
    public OrderAggregate create() {
        OrderAggregate aggregate = new OrderAggregate(UUID.randomUUID());
        return aggregate;
    }

    /**
     * Exception thrown when aggregate is not found.
     */
    public static class AggregateNotFoundException extends RuntimeException {
        public AggregateNotFoundException(String message) {
            super(message);
        }
    }
}
