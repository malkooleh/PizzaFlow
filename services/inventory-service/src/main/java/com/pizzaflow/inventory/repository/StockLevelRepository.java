package com.pizzaflow.inventory.repository;

import com.pizzaflow.inventory.model.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockLevelRepository extends JpaRepository<StockLevel, UUID> {

    Optional<StockLevel> findByIngredientIdAndRestaurantId(UUID ingredientId, UUID restaurantId);

    List<StockLevel> findByRestaurantId(UUID restaurantId);

    @Query("SELECT sl FROM StockLevel sl JOIN FETCH sl.ingredient WHERE sl.restaurantId = :restaurantId")
    List<StockLevel> findByRestaurantIdWithIngredient(@Param("restaurantId") UUID restaurantId);

    @Query("""
            SELECT sl FROM StockLevel sl
            JOIN FETCH sl.ingredient i
            WHERE sl.restaurantId = :restaurantId
            AND sl.availableQuantity <= i.minimumStockLevel
            """)
    List<StockLevel> findLowStockByRestaurant(@Param("restaurantId") UUID restaurantId);

    @Modifying
    @Query("""
            UPDATE StockLevel sl
            SET sl.reservedQuantity = sl.reservedQuantity + :quantity
            WHERE sl.id = :id
            AND sl.currentQuantity - (sl.reservedQuantity + :quantity) >= 0
            """)
    int reserveStock(@Param("id") UUID id, @Param("quantity") BigDecimal quantity);

    @Modifying
    @Query("""
            UPDATE StockLevel sl
            SET sl.reservedQuantity = sl.reservedQuantity - :quantity
            WHERE sl.id = :id
            AND sl.reservedQuantity >= :quantity
            """)
    int releaseReservedStock(@Param("id") UUID id, @Param("quantity") BigDecimal quantity);

    @Modifying
    @Query("""
            UPDATE StockLevel sl
            SET sl.currentQuantity = sl.currentQuantity - :quantity,
                sl.reservedQuantity = sl.reservedQuantity - :quantity
            WHERE sl.id = :id
            AND sl.reservedQuantity >= :quantity
            AND sl.currentQuantity >= :quantity
            """)
    int consumeReservedStock(@Param("id") UUID id, @Param("quantity") BigDecimal quantity);
}
