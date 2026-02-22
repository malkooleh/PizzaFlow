package com.pizzaflow.kitchen.service;

import com.pizzaflow.kitchen.dto.KitchenOrderDTO;
import com.pizzaflow.kitchen.dto.QueueStatusDTO;
import com.pizzaflow.kitchen.event.PaymentCompletedEvent;
import com.pizzaflow.kitchen.model.KitchenOrder;
import com.pizzaflow.kitchen.model.KitchenOrderItem;
import com.pizzaflow.kitchen.model.enums.KitchenOrderStatus;
import com.pizzaflow.kitchen.model.enums.OrderPriority;
import com.pizzaflow.kitchen.repository.KitchenOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KitchenQueueService {

    private final KitchenOrderRepository kitchenOrderRepository;
    private final WebSocketService webSocketService;
    private final KafkaProducerService kafkaProducerService;

    // Average prep time per item type (minutes)
    private static final int DEFAULT_ITEM_PREP_TIME = 10;
    private static final int PIZZA_PREP_TIME = 15;

    /**
     * Add a new order to the kitchen queue after payment is confirmed.
     */
    public KitchenOrderDTO addOrderToQueue(PaymentCompletedEvent event) {
        log.info("Adding order to kitchen queue: orderId={}", event.getOrderId());

        // Check if order already exists
        if (kitchenOrderRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Order already exists in kitchen queue: {}", event.getOrderId());
            return getKitchenOrder(event.getOrderId());
        }

        // Convert event items to kitchen items
        List<KitchenOrderItem> kitchenItems = event.getItems() != null
                ? event.getItems().stream()
                        .map(item -> KitchenOrderItem.builder()
                                .menuItemId(item.getMenuItemId())
                                .menuItemName(item.getMenuItemName())
                                .quantity(item.getQuantity())
                                .customizations(item.getCustomizations())
                                .specialInstructions(item.getSpecialInstructions())
                                .prepTimeMinutes(estimateItemPrepTime(item.getMenuItemName()))
                                .build())
                        .collect(Collectors.toList())
                : Collections.emptyList();

        // Calculate priority and estimated prep time
        OrderPriority priority = determinePriority(event.getOrderType());
        int estimatedPrepTime = calculateEstimatedPrepTime(kitchenItems);
        int queuePosition = calculateQueuePosition(event.getRestaurantId(), priority);

        // Create kitchen order
        KitchenOrder kitchenOrder = KitchenOrder.builder()
                .id(UUID.randomUUID().toString())
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .restaurantId(event.getRestaurantId())
                .customerId(event.getCustomerId())
                .orderType(event.getOrderType())
                .status(KitchenOrderStatus.RECEIVED)
                .priority(priority)
                .items(kitchenItems)
                .estimatedPrepTimeMinutes(estimatedPrepTime)
                .receivedAt(LocalDateTime.now())
                .specialInstructions(event.getSpecialInstructions())
                .queuePosition(queuePosition)
                .build();

        KitchenOrder saved = kitchenOrderRepository.save(kitchenOrder);
        log.info("Order added to queue: orderId={}, queuePosition={}", event.getOrderId(), queuePosition);

        // Broadcast new order to kitchen displays
        webSocketService.notifyNewOrder(
                event.getRestaurantId(),
                event.getOrderId(),
                event.getOrderNumber(),
                queuePosition,
                estimatedPrepTime);

        return toDTO(saved);
    }

    /**
     * Start preparing an order.
     */
    public KitchenOrderDTO startPreparing(Long orderId, String assignedStation) {
        log.info("Starting preparation: orderId={}, station={}", orderId, assignedStation);

        KitchenOrder order = kitchenOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Kitchen order not found: " + orderId));

        if (order.getStatus() != KitchenOrderStatus.RECEIVED) {
            throw new IllegalStateException("Order cannot be started from status: " + order.getStatus());
        }

        KitchenOrderStatus previousStatus = order.getStatus();
        order.setStatus(KitchenOrderStatus.PREPARING);
        order.setStartedAt(LocalDateTime.now());
        order.setAssignedStation(assignedStation);

        KitchenOrder saved = kitchenOrderRepository.save(order);

        // Publish Kafka event
        kafkaProducerService.publishOrderPreparing(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getRestaurantId(),
                order.getEstimatedPrepTimeMinutes(),
                assignedStation);

        // Broadcast status change
        webSocketService.notifyStatusChange(
                order.getRestaurantId(),
                order.getOrderId(),
                order.getOrderNumber(),
                previousStatus,
                KitchenOrderStatus.PREPARING);

        // Recalculate queue positions
        recalculateQueuePositions(order.getRestaurantId());

        return toDTO(saved);
    }

    /**
     * Mark order as ready.
     */
    public KitchenOrderDTO markReady(Long orderId) {
        log.info("Marking order as ready: orderId={}", orderId);

        KitchenOrder order = kitchenOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Kitchen order not found: " + orderId));

        if (order.getStatus() != KitchenOrderStatus.PREPARING) {
            throw new IllegalStateException("Order cannot be marked ready from status: " + order.getStatus());
        }

        KitchenOrderStatus previousStatus = order.getStatus();
        order.setStatus(KitchenOrderStatus.READY);
        order.setCompletedAt(LocalDateTime.now());

        int actualPrepTime = (int) ChronoUnit.MINUTES.between(order.getStartedAt(), order.getCompletedAt());

        KitchenOrder saved = kitchenOrderRepository.save(order);

        // Publish Kafka event
        kafkaProducerService.publishOrderReady(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getRestaurantId(),
                order.getCustomerId(),
                order.getOrderType(),
                actualPrepTime);

        // Broadcast status change
        webSocketService.notifyStatusChange(
                order.getRestaurantId(),
                order.getOrderId(),
                order.getOrderNumber(),
                previousStatus,
                KitchenOrderStatus.READY);

        return toDTO(saved);
    }

    /**
     * Mark order as picked up (removes from active queue).
     */
    public KitchenOrderDTO markPickedUp(Long orderId) {
        log.info("Marking order as picked up: orderId={}", orderId);

        KitchenOrder order = kitchenOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Kitchen order not found: " + orderId));

        if (order.getStatus() != KitchenOrderStatus.READY) {
            throw new IllegalStateException("Order cannot be picked up from status: " + order.getStatus());
        }

        order.setStatus(KitchenOrderStatus.PICKED_UP);
        KitchenOrder saved = kitchenOrderRepository.save(order);

        // Broadcast removal
        webSocketService.notifyOrderRemoved(
                order.getRestaurantId(),
                order.getOrderId(),
                order.getOrderNumber(),
                KitchenOrderStatus.PICKED_UP);

        return toDTO(saved);
    }

    /**
     * Get kitchen order by order ID.
     */
    public KitchenOrderDTO getKitchenOrder(Long orderId) {
        return kitchenOrderRepository.findByOrderId(orderId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Kitchen order not found: " + orderId));
    }

    /**
     * Get queue status for a restaurant.
     */
    public QueueStatusDTO getQueueStatus(Long restaurantId) {
        List<KitchenOrderStatus> activeStatuses = List.of(
                KitchenOrderStatus.RECEIVED,
                KitchenOrderStatus.PREPARING,
                KitchenOrderStatus.READY);

        List<KitchenOrder> activeOrders = kitchenOrderRepository
                .findByRestaurantIdAndStatusIn(restaurantId, activeStatuses);

        // Sort by priority (HIGH first) and then by received time
        List<KitchenOrderDTO> sortedOrders = activeOrders.stream()
                .sorted(Comparator
                        .comparing(KitchenOrder::getPriority, Comparator.reverseOrder())
                        .thenComparing(KitchenOrder::getReceivedAt))
                .map(this::toDTO)
                .collect(Collectors.toList());

        int receivedCount = (int) activeOrders.stream()
                .filter(o -> o.getStatus() == KitchenOrderStatus.RECEIVED).count();
        int preparingCount = (int) activeOrders.stream()
                .filter(o -> o.getStatus() == KitchenOrderStatus.PREPARING).count();
        int readyCount = (int) activeOrders.stream()
                .filter(o -> o.getStatus() == KitchenOrderStatus.READY).count();

        int avgWaitTime = calculateAverageWaitTime(activeOrders);

        return QueueStatusDTO.builder()
                .restaurantId(restaurantId)
                .totalOrders(activeOrders.size())
                .receivedCount(receivedCount)
                .preparingCount(preparingCount)
                .readyCount(readyCount)
                .averageWaitTimeMinutes(avgWaitTime)
                .orders(sortedOrders)
                .build();
    }

    private OrderPriority determinePriority(String orderType) {
        if (orderType == null)
            return OrderPriority.NORMAL;
        return switch (orderType.toUpperCase()) {
            case "ASAP", "DELIVERY" -> OrderPriority.HIGH;
            case "SCHEDULED" -> OrderPriority.LOW;
            case "DINE_IN" -> OrderPriority.NORMAL;
            default -> OrderPriority.NORMAL;
        };
    }

    private int estimateItemPrepTime(String itemName) {
        if (itemName == null)
            return DEFAULT_ITEM_PREP_TIME;
        if (itemName.toLowerCase().contains("pizza"))
            return PIZZA_PREP_TIME;
        return DEFAULT_ITEM_PREP_TIME;
    }

    private int calculateEstimatedPrepTime(List<KitchenOrderItem> items) {
        if (items == null || items.isEmpty())
            return DEFAULT_ITEM_PREP_TIME;

        // Prep time is based on the item that takes longest (parallel cooking)
        return items.stream()
                .mapToInt(item -> item.getPrepTimeMinutes() * item.getQuantity())
                .max()
                .orElse(DEFAULT_ITEM_PREP_TIME);
    }

    private int calculateQueuePosition(Long restaurantId, OrderPriority priority) {
        List<KitchenOrder> receivedOrders = kitchenOrderRepository
                .findByRestaurantIdAndStatus(restaurantId, KitchenOrderStatus.RECEIVED);

        // Count orders with same or higher priority
        return (int) receivedOrders.stream()
                .filter(o -> o.getPriority().ordinal() >= priority.ordinal())
                .count() + 1;
    }

    private void recalculateQueuePositions(Long restaurantId) {
        List<KitchenOrder> receivedOrders = kitchenOrderRepository
                .findByRestaurantIdAndStatus(restaurantId, KitchenOrderStatus.RECEIVED);

        List<KitchenOrder> sorted = receivedOrders.stream()
                .sorted(Comparator
                        .comparing(KitchenOrder::getPriority, Comparator.reverseOrder())
                        .thenComparing(KitchenOrder::getReceivedAt))
                .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setQueuePosition(i + 1);
        }

        kitchenOrderRepository.saveAll(sorted);
        webSocketService.notifyQueueUpdate(restaurantId);
    }

    private int calculateAverageWaitTime(List<KitchenOrder> orders) {
        if (orders.isEmpty())
            return 0;

        int totalPrepTime = orders.stream()
                .filter(o -> o.getStatus() == KitchenOrderStatus.RECEIVED)
                .mapToInt(KitchenOrder::getEstimatedPrepTimeMinutes)
                .sum();

        int receivedCount = (int) orders.stream()
                .filter(o -> o.getStatus() == KitchenOrderStatus.RECEIVED)
                .count();

        return receivedCount > 0 ? totalPrepTime / receivedCount : 0;
    }

    private KitchenOrderDTO toDTO(KitchenOrder order) {
        return KitchenOrderDTO.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .restaurantId(order.getRestaurantId())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .priority(order.getPriority())
                .items(order.getItems())
                .estimatedPrepTimeMinutes(order.getEstimatedPrepTimeMinutes())
                .scheduledTime(order.getScheduledTime())
                .receivedAt(order.getReceivedAt())
                .startedAt(order.getStartedAt())
                .completedAt(order.getCompletedAt())
                .specialInstructions(order.getSpecialInstructions())
                .queuePosition(order.getQueuePosition())
                .assignedStation(order.getAssignedStation())
                .build();
    }
}
