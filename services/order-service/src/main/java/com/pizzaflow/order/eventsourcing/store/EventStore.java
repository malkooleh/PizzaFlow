package com.pizzaflow.order.eventsourcing.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaflow.order.eventsourcing.event.OrderDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for storing and retrieving events from the event store.
 * Handles serialization/deserialization and optimistic concurrency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventStore {

    private final EventStoreRepository eventStoreRepository;
    private final SnapshotRepository snapshotRepository;
    private final ObjectMapper objectMapper;

    private static final int SNAPSHOT_THRESHOLD = 10; // Create snapshot every 10 events

    /**
     * Append a list of events to the event store for an aggregate.
     * 
     * @param aggregateId     The aggregate identifier
     * @param events          List of events to append
     * @param expectedVersion Expected current version for optimistic concurrency
     * @throws ConcurrencyException if the expected version doesn't match
     */
    @Transactional
    public void appendEvents(UUID aggregateId, List<OrderDomainEvent> events, Long expectedVersion) {
        log.debug("Appending {} events for aggregate {}, expected version: {}",
                events.size(), aggregateId, expectedVersion);

        // Check current version for optimistic concurrency
        Optional<Long> currentVersion = eventStoreRepository.findMaxVersionByAggregateId(aggregateId);
        Long actualVersion = currentVersion.orElse(0L);

        if (!actualVersion.equals(expectedVersion)) {
            throw new ConcurrencyException(
                    String.format("Concurrency conflict for aggregate %s. Expected version %d, but found %d",
                            aggregateId, expectedVersion, actualVersion));
        }

        long version = expectedVersion;
        for (OrderDomainEvent event : events) {
            version++;
            event.setVersion(version);
            event.initializeEvent();

            try {
                StoredEvent storedEvent = StoredEvent.builder()
                        .aggregateId(aggregateId)
                        .aggregateType("Order")
                        .eventType(event.getEventType())
                        .eventData(objectMapper.writeValueAsString(event))
                        .version(version)
                        .timestamp(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
                        .triggeredBy(event.getTriggeredBy())
                        .correlationId(event.getCorrelationId())
                        .build();

                eventStoreRepository.save(storedEvent);
                log.debug("Stored event {} version {} for aggregate {}",
                        event.getEventType(), version, aggregateId);

            } catch (JsonProcessingException e) {
                throw new EventStoreException("Failed to serialize event: " + event.getEventType(), e);
            } catch (DataIntegrityViolationException e) {
                throw new ConcurrencyException(
                        "Concurrent modification detected for aggregate: " + aggregateId, e);
            }
        }
    }

    /**
     * Load all events for an aggregate.
     */
    @Transactional(readOnly = true)
    public List<OrderDomainEvent> loadEvents(UUID aggregateId) {
        List<StoredEvent> storedEvents = eventStoreRepository.findByAggregateIdOrderByVersionAsc(aggregateId);
        return deserializeEvents(storedEvents);
    }

    /**
     * Load events for an aggregate from a specific version.
     * Useful when replaying from a snapshot.
     */
    @Transactional(readOnly = true)
    public List<OrderDomainEvent> loadEventsFromVersion(UUID aggregateId, Long fromVersion) {
        List<StoredEvent> storedEvents = eventStoreRepository
                .findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(aggregateId, fromVersion);
        return deserializeEvents(storedEvents);
    }

    /**
     * Get the current version of an aggregate.
     */
    @Transactional(readOnly = true)
    public Long getCurrentVersion(UUID aggregateId) {
        return eventStoreRepository.findMaxVersionByAggregateId(aggregateId).orElse(0L);
    }

    /**
     * Check if an aggregate exists.
     */
    @Transactional(readOnly = true)
    public boolean aggregateExists(UUID aggregateId) {
        return eventStoreRepository.existsByAggregateId(aggregateId);
    }

    /**
     * Save a snapshot of aggregate state.
     */
    @Transactional
    public void saveSnapshot(UUID aggregateId, Object aggregateState, Long version) {
        try {
            AggregateSnapshot snapshot = AggregateSnapshot.builder()
                    .aggregateId(aggregateId)
                    .aggregateType("Order")
                    .version(version)
                    .snapshotData(objectMapper.writeValueAsString(aggregateState))
                    .createdAt(Instant.now())
                    .build();

            snapshotRepository.save(snapshot);
            log.debug("Saved snapshot for aggregate {} at version {}", aggregateId, version);

        } catch (JsonProcessingException e) {
            throw new EventStoreException("Failed to serialize snapshot for aggregate: " + aggregateId, e);
        }
    }

    /**
     * Load the latest snapshot for an aggregate.
     */
    @Transactional(readOnly = true)
    public <T> Optional<SnapshotWithVersion<T>> loadLatestSnapshot(UUID aggregateId, Class<T> type) {
        return snapshotRepository.findLatestByAggregateId(aggregateId)
                .map(snapshot -> {
                    try {
                        T state = objectMapper.readValue(snapshot.getSnapshotData(), type);
                        return new SnapshotWithVersion<>(state, snapshot.getVersion());
                    } catch (JsonProcessingException e) {
                        throw new EventStoreException("Failed to deserialize snapshot", e);
                    }
                });
    }

    /**
     * Check if a snapshot should be created based on event count.
     */
    public boolean shouldCreateSnapshot(UUID aggregateId) {
        long eventCount = eventStoreRepository.countByAggregateId(aggregateId);
        Optional<Long> latestSnapshotVersion = snapshotRepository.findLatestByAggregateId(aggregateId)
                .map(AggregateSnapshot::getVersion);

        long eventsSinceSnapshot = latestSnapshotVersion
                .map(v -> eventCount - v)
                .orElse(eventCount);

        return eventsSinceSnapshot >= SNAPSHOT_THRESHOLD;
    }

    private List<OrderDomainEvent> deserializeEvents(List<StoredEvent> storedEvents) {
        return storedEvents.stream()
                .map(this::deserializeEvent)
                .collect(Collectors.toList());
    }

    private OrderDomainEvent deserializeEvent(StoredEvent storedEvent) {
        try {
            return objectMapper.readValue(storedEvent.getEventData(), OrderDomainEvent.class);
        } catch (JsonProcessingException e) {
            throw new EventStoreException(
                    "Failed to deserialize event: " + storedEvent.getEventType(), e);
        }
    }

    /**
     * Wrapper for snapshot with its version.
     */
    public record SnapshotWithVersion<T>(T state, Long version) {
    }

    /**
     * Exception for event store operations.
     */
    public static class EventStoreException extends RuntimeException {
        public EventStoreException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception for concurrency conflicts.
     */
    public static class ConcurrencyException extends RuntimeException {
        public ConcurrencyException(String message) {
            super(message);
        }

        public ConcurrencyException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
