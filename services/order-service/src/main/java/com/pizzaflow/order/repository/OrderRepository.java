package com.pizzaflow.order.repository;

import com.pizzaflow.order.model.Order;
import com.pizzaflow.order.model.enums.OrderStatus;
import com.pizzaflow.order.model.enums.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByCustomerId(Long customerId);
    
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    
    List<Order> findByRestaurantId(Long restaurantId);
    
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByOrderType(OrderType orderType);
    
    @Query("SELECT o FROM Order o WHERE o.scheduledTime BETWEEN :startTime AND :endTime")
    List<Order> findScheduledOrders(@Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId " +
           "AND o.status IN ('PENDING', 'CONFIRMED', 'PREPARING') " +
           "ORDER BY o.createdAt ASC")
    List<Order> findActiveOrdersByRestaurant(@Param("restaurantId") Long restaurantId);
}
