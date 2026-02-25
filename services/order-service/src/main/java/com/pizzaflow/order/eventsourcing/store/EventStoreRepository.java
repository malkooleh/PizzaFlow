package com.pizzaflow.order.eventsourcing.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing stored events in the event store.
 */
@Repository
public interface EventStoreRepository extends JpaRepository<StoredEvent, UUID> {

    /**
     * Find all events for an aggregate, ordered by version
     */
    List<StoredEvent> findByAggregateIdOrderByVersionAsc(UUID aggregateId);

    /**
     * Find all events for an aggregate after a specific version
     */
    @Query("SELECT e FROM StoredEvent e WHERE e.aggregateId = :aggregateId AND e.version > :fromVersion ORDER BY e.version ASC")
    List<StoredEvent> findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(
            @Param("aggregateId") UUID aggregateId,
            @Param("fromVersion") Long fromVersion);

    /**
     * Find the latest version number for an aggregate
     */
    @Query("SELECT MAX(e.version) FROM StoredEvent e WHERE e.aggregateId = :aggregateId")
    Optional<Long> findMaxVersionByAggregateId(@Param("aggregateId") UUID aggregateId);

    /**
     * Find all events of a specific type
     */
    List<StoredEvent> findByEventTypeOrderByTimestampDesc(String eventType);

    /**
     * Find events by correlation ID
     */
    List<StoredEvent> findByCorrelationIdOrderByTimestampAsc(String correlationId);

    /**
     * Find events within a time range for replay
     */
    @Query("SELECT e FROM StoredEvent e WHERE e.timestamp >= :from AND e.timestamp <= :to ORDER BY e.timestamp ASC")
    List<StoredEvent> findByTimestampBetween(@Param("from") Instant from, @Param("to") Instant to);

    /**
     * Count events for an aggregate
     */
    long countByAggregateId(UUID aggregateId);

    /**
     * Check if aggregate exists
     */
    boolean existsByAggregateId(UUID aggregateId);
}
