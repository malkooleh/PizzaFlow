package com.pizzaflow.order.eventsourcing.service;

import com.pizzaflow.order.eventsourcing.readmodel.OrderReadModel;
import com.pizzaflow.order.eventsourcing.readmodel.OrderReadModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Query service for order queries (CQRS read side).
 * All queries go through the denormalized read model for optimal performance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderReadModelRepository readModelRepository;

    /**
     * Get order by ID.
     */
    public OrderReadModel getOrder(UUID orderId) {
        log.debug("Querying order by ID: {}", orderId);
        return readModelRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    /**
     * Get order by ID with items eagerly loaded.
     */
    public OrderReadModel getOrderWithItems(UUID orderId) {
        log.debug("Querying order with items by ID: {}", orderId);
        return readModelRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    /**
     * Get order by order number.
     */
    public OrderReadModel getOrderByNumber(String orderNumber) {
        log.debug("Querying order by number: {}", orderNumber);
        return readModelRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
    }

    /**
     * Get all orders for a customer.
     */
    public List<OrderReadModel> getCustomerOrders(UUID customerId) {
        log.debug("Querying orders for customer: {}", customerId);
        return readModelRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Get paginated orders for a customer.
     */
    public Page<OrderReadModel> getCustomerOrders(UUID customerId, Pageable pageable) {
        log.debug("Querying paginated orders for customer: {}", customerId);
        return readModelRepository.findByCustomerId(customerId, pageable);
    }

    /**
     * Get orders for a restaurant.
     */
    public Page<OrderReadModel> getRestaurantOrders(UUID restaurantId, Pageable pageable) {
        log.debug("Querying orders for restaurant: {}", restaurantId);
        return readModelRepository.findByRestaurantId(restaurantId, pageable);
    }

    /**
     * Get orders by status.
     */
    public List<OrderReadModel> getOrdersByStatus(String status) {
        log.debug("Querying orders by status: {}", status);
        return readModelRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Get paginated orders by status.
     */
    public Page<OrderReadModel> getOrdersByStatus(String status, Pageable pageable) {
        log.debug("Querying paginated orders by status: {}", status);
        return readModelRepository.findByStatus(status, pageable);
    }

    /**
     * Get pending orders for kitchen display (CONFIRMED and PREPARING).
     */
    public List<OrderReadModel> getPendingOrdersForKitchen(UUID restaurantId) {
        log.debug("Querying pending orders for kitchen display: {}", restaurantId);
        return readModelRepository.findPendingOrdersForRestaurant(restaurantId);
    }

    /**
     * Get orders ready for pickup/delivery.
     */
    public List<OrderReadModel> getReadyOrders(UUID restaurantId) {
        log.debug("Querying ready orders for restaurant: {}", restaurantId);
        return readModelRepository.findReadyOrdersForRestaurant(restaurantId);
    }

    /**
     * Get active deliveries for a courier.
     */
    public List<OrderReadModel> getActiveDeliveries(UUID courierId) {
        log.debug("Querying active deliveries for courier: {}", courierId);
        return readModelRepository.findActiveDeliveriesForCourier(courierId);
    }

    /**
     * Get scheduled orders within a time window.
     */
    public List<OrderReadModel> getScheduledOrders(Instant from, Instant to) {
        log.debug("Querying scheduled orders from {} to {}", from, to);
        return readModelRepository.findScheduledOrdersInWindow(from, to);
    }

    /**
     * Get order count by status for a restaurant (dashboard).
     */
    public Map<String, Long> getOrderCountByStatus(UUID restaurantId) {
        log.debug("Querying order count by status for restaurant: {}", restaurantId);
        List<Object[]> results = readModelRepository.countByStatusForRestaurant(restaurantId);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));
    }

    /**
     * Get orders created within a time range.
     */
    public Page<OrderReadModel> getOrdersByDateRange(Instant from, Instant to, Pageable pageable) {
        log.debug("Querying orders from {} to {}", from, to);
        return readModelRepository.findByCreatedAtBetween(from, to, pageable);
    }

    /**
     * Search orders by order number pattern.
     */
    public List<OrderReadModel> searchOrders(String pattern) {
        log.debug("Searching orders with pattern: {}", pattern);
        return readModelRepository.searchByOrderNumber(pattern);
    }

    /**
     * Get daily order statistics.
     */
    public List<DailyOrderStats> getDailyStats(Instant from) {
        log.debug("Querying daily order stats from: {}", from);
        List<Object[]> results = readModelRepository.getDailyOrderStats(from);
        return results.stream()
                .map(row -> new DailyOrderStats(
                        row[0].toString(),
                        ((Number) row[1]).longValue(),
                        new java.math.BigDecimal(row[2].toString())))
                .collect(Collectors.toList());
    }

    /**
     * DTO for daily order statistics.
     */
    public record DailyOrderStats(String date, Long totalOrders, java.math.BigDecimal totalRevenue) {
    }

    /**
     * Exception thrown when order is not found.
     */
    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }
}
