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
 * JPA entity representing a stored event in the event store.
 */
@Entity
@Table(name = "event_store", indexes = {
        @Index(name = "idx_event_store_aggregate_id", columnList = "aggregate_id"),
        @Index(name = "idx_event_store_event_type", columnList = "event_type"),
        @Index(name = "idx_event_store_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    @Builder.Default
    private String aggregateType = "Order";

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "event_data", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String eventData;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "triggered_by", length = 255)
    private String triggeredBy;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}
