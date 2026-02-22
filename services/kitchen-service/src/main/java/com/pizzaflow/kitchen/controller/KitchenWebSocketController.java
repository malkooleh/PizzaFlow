package com.pizzaflow.kitchen.controller;

import com.pizzaflow.kitchen.dto.KitchenOrderDTO;
import com.pizzaflow.kitchen.dto.QueueStatusDTO;
import com.pizzaflow.kitchen.service.KitchenQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for Kitchen Display System.
 * Handles STOMP messages from kitchen displays.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class KitchenWebSocketController {

    private final KitchenQueueService kitchenQueueService;

    /**
     * Handle request for queue status.
     * Client sends to /app/kitchen/{restaurantId}/status
     * Response goes to /topic/kitchen/{restaurantId}
     */
    @MessageMapping("/kitchen/{restaurantId}/status")
    @SendTo("/topic/kitchen/{restaurantId}")
    public QueueStatusDTO getQueueStatus(@DestinationVariable Long restaurantId) {
        log.debug("WebSocket: Queue status request for restaurant: {}", restaurantId);
        return kitchenQueueService.getQueueStatus(restaurantId);
    }

    /**
     * Handle order status update from kitchen display.
     * Client sends to /app/kitchen/{restaurantId}/order/{orderId}/start
     */
    @MessageMapping("/kitchen/{restaurantId}/order/{orderId}/start")
    @SendTo("/topic/kitchen/{restaurantId}")
    public KitchenOrderDTO startOrder(
            @DestinationVariable Long restaurantId,
            @DestinationVariable Long orderId,
            @Payload(required = false) String station) {
        log.info("WebSocket: Starting order {} at station {}", orderId, station);
        return kitchenQueueService.startPreparing(orderId, station);
    }

    /**
     * Handle order ready notification from kitchen display.
     * Client sends to /app/kitchen/{restaurantId}/order/{orderId}/ready
     */
    @MessageMapping("/kitchen/{restaurantId}/order/{orderId}/ready")
    @SendTo("/topic/kitchen/{restaurantId}")
    public KitchenOrderDTO markOrderReady(
            @DestinationVariable Long restaurantId,
            @DestinationVariable Long orderId) {
        log.info("WebSocket: Marking order {} as ready", orderId);
        return kitchenQueueService.markReady(orderId);
    }

    /**
     * Handle order pickup notification.
     * Client sends to /app/kitchen/{restaurantId}/order/{orderId}/pickup
     */
    @MessageMapping("/kitchen/{restaurantId}/order/{orderId}/pickup")
    @SendTo("/topic/kitchen/{restaurantId}")
    public KitchenOrderDTO markOrderPickedUp(
            @DestinationVariable Long restaurantId,
            @DestinationVariable Long orderId) {
        log.info("WebSocket: Marking order {} as picked up", orderId);
        return kitchenQueueService.markPickedUp(orderId);
    }
}
