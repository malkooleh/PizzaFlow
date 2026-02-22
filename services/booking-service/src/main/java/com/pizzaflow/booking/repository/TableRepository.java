package com.pizzaflow.booking.repository;

import com.pizzaflow.booking.model.TableConfiguration;
import com.pizzaflow.booking.model.enums.TableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TableRepository extends JpaRepository<TableConfiguration, UUID> {

    List<TableConfiguration> findByRestaurantIdAndIsActiveTrue(UUID restaurantId);

    @Query("""
        SELECT t FROM TableConfiguration t 
        WHERE t.restaurant.id = :restaurantId 
        AND t.isActive = true 
        AND t.capacity >= :partySize 
        AND t.minCapacity <= :partySize
        ORDER BY t.capacity ASC
        """)
    List<TableConfiguration> findSuitableTablesForPartySize(
        @Param("restaurantId") UUID restaurantId,
        @Param("partySize") int partySize
    );

    @Query("""
        SELECT t FROM TableConfiguration t 
        WHERE t.restaurant.id = :restaurantId 
        AND t.isActive = true 
        AND t.tableType = :tableType
        """)
    List<TableConfiguration> findByRestaurantIdAndTableType(
        @Param("restaurantId") UUID restaurantId,
        @Param("tableType") TableType tableType
    );

    @Query("""
        SELECT SUM(t.capacity) FROM TableConfiguration t 
        WHERE t.restaurant.id = :restaurantId 
        AND t.isActive = true
        """)
    Integer getTotalCapacity(@Param("restaurantId") UUID restaurantId);
}
