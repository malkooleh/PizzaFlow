package com.pizzaflow.order.eventsourcing.readmodel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for querying the order read model.
 * Optimized for various query patterns in the CQRS read side.
 */
@Repository
public interface OrderReadModelRepository extends JpaRepository<OrderReadModel, UUID> {

    /**
     * Find by order number
     */
    Optional<OrderReadModel> findByOrderNumber(String orderNumber);

    /**
     * Find orders by customer, sorted by creation date
     */
    List<OrderReadModel> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    /**
     * Find orders by customer with pagination
     */
    Page<OrderReadModel> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Find orders by restaurant
     */
    Page<OrderReadModel> findByRestaurantId(UUID restaurantId, Pageable pageable);

    /**
     * Find orders by status
     */
    List<OrderReadModel> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find orders by status with pagination
     */
    Page<OrderReadModel> findByStatus(String status, Pageable pageable);

    /**
     * Find orders by restaurant and status
     */
    List<OrderReadModel> findByRestaurantIdAndStatusOrderByCreatedAtAsc(UUID restaurantId, String status);

    /**
     * Find pending orders for a restaurant (kitchen display)
     */
    @Query("SELECT o FROM OrderReadModel o WHERE o.restaurantId = :restaurantId " +
            "AND o.status IN ('CONFIRMED', 'PREPARING') ORDER BY o.createdAt ASC")
    List<OrderReadModel> findPendingOrdersForRestaurant(@Param("restaurantId") UUID restaurantId);

    /**
     * Find orders ready for pickup
     */
    @Query("SELECT o FROM OrderReadModel o WHERE o.restaurantId = :restaurantId " +
            "AND o.status = 'READY' ORDER BY o.readyAt ASC")
    List<OrderReadModel> findReadyOrdersForRestaurant(@Param("restaurantId") UUID restaurantId);

    /**
     * Find active deliveries for a courier
     */
    @Query("SELECT o FROM OrderReadModel o WHERE o.courierId = :courierId " +
            "AND o.status = 'OUT_FOR_DELIVERY' ORDER BY o.pickedUpAt ASC")
    List<OrderReadModel> findActiveDeliveriesForCourier(@Param("courierId") UUID courierId);

    /**
     * Find scheduled orders for upcoming time window
     */
    @Query("SELECT o FROM OrderReadModel o WHERE o.scheduledTime BETWEEN :from AND :to " +
            "AND o.status IN ('PENDING', 'CONFIRMED') ORDER BY o.scheduledTime ASC")
    List<OrderReadModel> findScheduledOrdersInWindow(
            @Param("from") Instant from,
            @Param("to") Instant to);

    /**
     * Count orders by status for a restaurant (dashboard)
     */
    @Query("SELECT o.status, COUNT(o) FROM OrderReadModel o WHERE o.restaurantId = :restaurantId " +
            "GROUP BY o.status")
    List<Object[]> countByStatusForRestaurant(@Param("restaurantId") UUID restaurantId);

    /**
     * Find orders created within a time range
     */
    @Query("SELECT o FROM OrderReadModel o WHERE o.createdAt BETWEEN :from AND :to " +
            "ORDER BY o.createdAt DESC")
    Page<OrderReadModel> findByCreatedAtBetween(
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);

    /**
     * Search orders by order number pattern
     */
    @Query("SELECT o FROM OrderReadModel o WHERE LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<OrderReadModel> searchByOrderNumber(@Param("pattern") String pattern);

    /**
     * Find orders with items (fetch join to avoid N+1)
     */
    @Query("SELECT DISTINCT o FROM OrderReadModel o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<OrderReadModel> findByIdWithItems(@Param("orderId") UUID orderId);

    /**
     * Aggregate daily order statistics
     */
    @Query(value = "SELECT DATE(created_at) as order_date, COUNT(*) as total_orders, " +
            "SUM(total_amount) as total_revenue FROM order_read_model " +
            "WHERE created_at >= :from GROUP BY DATE(created_at) ORDER BY order_date DESC", nativeQuery = true)
    List<Object[]> getDailyOrderStats(@Param("from") Instant from);
}
