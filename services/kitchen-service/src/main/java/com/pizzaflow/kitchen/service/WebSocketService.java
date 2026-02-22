package com.pizzaflow.kitchen.service;

import com.pizzaflow.kitchen.dto.OrderUpdateDTO;
import com.pizzaflow.kitchen.model.enums.KitchenOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for broadcasting real-time updates to Kitchen Display Systems via
 * WebSocket.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast order update to all subscribers of a restaurant's kitchen display.
     */
    public void broadcastOrderUpdate(Long restaurantId, OrderUpdateDTO update) {
        String destination = "/topic/kitchen/" + restaurantId;
        log.debug("Broadcasting to {}: {}", destination, update);
        messagingTemplate.convertAndSend(destination, update);
    }

    /**
     * Broadcast new order arrival.
     */
    public void notifyNewOrder(Long restaurantId, Long orderId, String orderNumber,
            Integer queuePosition, Integer estimatedPrepTime) {
        OrderUpdateDTO update = OrderUpdateDTO.builder()
                .updateType("NEW_ORDER")
                .orderId(orderId)
                .orderNumber(orderNumber)
                .restaurantId(restaurantId)
                .status(KitchenOrderStatus.RECEIVED)
                .queuePosition(queuePosition)
                .estimatedPrepTimeMinutes(estimatedPrepTime)
                .timestamp(LocalDateTime.now())
                .build();

        broadcastOrderUpdate(restaurantId, update);
    }

    /**
     * Broadcast status change.
     */
    public void notifyStatusChange(Long restaurantId, Long orderId, String orderNumber,
            KitchenOrderStatus previousStatus, KitchenOrderStatus newStatus) {
        OrderUpdateDTO update = OrderUpdateDTO.builder()
                .updateType("STATUS_CHANGE")
                .orderId(orderId)
                .orderNumber(orderNumber)
                .restaurantId(restaurantId)
                .status(newStatus)
                .previousStatus(previousStatus)
                .timestamp(LocalDateTime.now())
                .build();

        broadcastOrderUpdate(restaurantId, update);
    }

    /**
     * Broadcast order removal from queue.
     */
    public void notifyOrderRemoved(Long restaurantId, Long orderId, String orderNumber,
            KitchenOrderStatus finalStatus) {
        OrderUpdateDTO update = OrderUpdateDTO.builder()
                .updateType("ORDER_REMOVED")
                .orderId(orderId)
                .orderNumber(orderNumber)
                .restaurantId(restaurantId)
                .status(finalStatus)
                .timestamp(LocalDateTime.now())
                .build();

        broadcastOrderUpdate(restaurantId, update);
    }

    /**
     * Broadcast queue reorder.
     */
    public void notifyQueueUpdate(Long restaurantId) {
        OrderUpdateDTO update = OrderUpdateDTO.builder()
                .updateType("QUEUE_UPDATE")
                .restaurantId(restaurantId)
                .timestamp(LocalDateTime.now())
                .build();

        broadcastOrderUpdate(restaurantId, update);
    }
}
