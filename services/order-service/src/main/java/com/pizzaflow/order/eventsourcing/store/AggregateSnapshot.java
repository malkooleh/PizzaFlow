package com.pizzaflow.order.eventsourcing.store;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a snapshot of aggregate state.
 */
@Entity
@Table(name = "aggregate_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregateSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    @Builder.Default
    private String aggregateType = "Order";

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "snapshot_data", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String snapshotData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
