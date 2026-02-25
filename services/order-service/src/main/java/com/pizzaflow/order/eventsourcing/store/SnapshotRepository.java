package com.pizzaflow.order.eventsourcing.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing aggregate snapshots.
 */
@Repository
public interface SnapshotRepository extends JpaRepository<AggregateSnapshot, UUID> {

    /**
     * Find the latest snapshot for an aggregate
     */
    @Query("SELECT s FROM AggregateSnapshot s WHERE s.aggregateId = :aggregateId ORDER BY s.version DESC LIMIT 1")
    Optional<AggregateSnapshot> findLatestByAggregateId(@Param("aggregateId") UUID aggregateId);

    /**
     * Find snapshot at or before a specific version
     */
    @Query("SELECT s FROM AggregateSnapshot s WHERE s.aggregateId = :aggregateId AND s.version <= :version ORDER BY s.version DESC LIMIT 1")
    Optional<AggregateSnapshot> findByAggregateIdAndVersionLessThanEqual(
            @Param("aggregateId") UUID aggregateId,
            @Param("version") Long version);

    /**
     * Delete old snapshots, keeping only the latest N
     */
    @Query("DELETE FROM AggregateSnapshot s WHERE s.aggregateId = :aggregateId AND s.version < :keepFromVersion")
    void deleteOldSnapshots(@Param("aggregateId") UUID aggregateId, @Param("keepFromVersion") Long keepFromVersion);
}
