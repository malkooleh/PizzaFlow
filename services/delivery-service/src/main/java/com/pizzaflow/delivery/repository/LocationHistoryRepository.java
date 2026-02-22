package com.pizzaflow.delivery.repository;

import com.pizzaflow.delivery.model.DeliveryLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocationHistoryRepository extends JpaRepository<DeliveryLocationHistory, UUID> {

    @Query("""
        SELECT lh FROM DeliveryLocationHistory lh 
        WHERE lh.delivery.id = :deliveryId 
        ORDER BY lh.recordedAt DESC
        """)
    List<DeliveryLocationHistory> findByDeliveryIdOrderByRecordedAtDesc(@Param("deliveryId") UUID deliveryId);

    @Query("""
        SELECT lh FROM DeliveryLocationHistory lh 
        WHERE lh.delivery.id = :deliveryId 
        ORDER BY lh.recordedAt DESC 
        LIMIT 1
        """)
    DeliveryLocationHistory findLatestByDeliveryId(@Param("deliveryId") UUID deliveryId);

    @Query("""
        DELETE FROM DeliveryLocationHistory lh 
        WHERE lh.recordedAt < :cutoff
        """)
    void deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
