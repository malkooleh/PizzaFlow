package com.pizzaflow.booking.repository;

import com.pizzaflow.booking.model.BlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, UUID> {

    @Query("""
        SELECT bs FROM BlockedSlot bs 
        WHERE bs.restaurant.id = :restaurantId 
        AND bs.startTime < :endTime 
        AND bs.endTime > :startTime
        AND (bs.table IS NULL OR bs.table.id = :tableId)
        """)
    List<BlockedSlot> findBlockedSlotsForTimeRange(
        @Param("restaurantId") UUID restaurantId,
        @Param("tableId") UUID tableId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT bs FROM BlockedSlot bs 
        WHERE bs.restaurant.id = :restaurantId 
        AND bs.table IS NULL
        AND bs.startTime < :endTime 
        AND bs.endTime > :startTime
        """)
    List<BlockedSlot> findRestaurantWideBlocks(
        @Param("restaurantId") UUID restaurantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
