package com.pizzaflow.inventory.repository;

import com.pizzaflow.inventory.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("""
            SELECT o FROM OutboxEvent o
            WHERE o.published = false
            ORDER BY o.createdAt ASC
            """)
    List<OutboxEvent> findUnpublishedEvents();

    @Query(value = """
            SELECT * FROM inventory_outbox
            WHERE published = false
            ORDER BY created_at ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findAndLockUnpublishedEvents(@Param("limit") int limit);

    @Modifying
    @Query("""
            UPDATE OutboxEvent o
            SET o.published = true, o.publishedAt = :publishedAt
            WHERE o.id = :id
            """)
    int markAsPublished(@Param("id") UUID id, @Param("publishedAt") LocalDateTime publishedAt);

    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.published = true AND o.publishedAt < :beforeTime")
    int deletePublishedEventsBefore(@Param("beforeTime") LocalDateTime beforeTime);
}
