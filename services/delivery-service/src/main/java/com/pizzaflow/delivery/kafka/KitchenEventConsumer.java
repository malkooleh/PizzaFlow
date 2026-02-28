package com.pizzaflow.delivery.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaflow.delivery.dto.CreateDeliveryRequest;
import com.pizzaflow.delivery.model.enums.DeliveryPriority;
import com.pizzaflow.delivery.service.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class KitchenEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KitchenEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final DeliveryService deliveryService;

    public KitchenEventConsumer(ObjectMapper objectMapper, DeliveryService deliveryService) {
        this.objectMapper = objectMapper;
        this.deliveryService = deliveryService;
    }

    /**
     * Listen for order.ready events from Kitchen Service
     * When an order is ready, create a delivery if it's for delivery.
     */
    @KafkaListener(topics = { "order.preparing", "order.ready" }, groupId = "delivery-service")
    public void handleKitchenEvent(String message, Acknowledgment ack) {
        JsonNode event;
        try {
            event = objectMapper.readTree(message);
        } catch (Exception e) {
            log.error("Failed to parse kitchen event JSON, skipping: {}", e.getMessage(), e);
            ack.acknowledge();
            return;
        }
        String eventType = event.get("eventType").asText();
        switch (eventType) {
            case "ORDER_READY" -> handleOrderReady(event);
            case "ORDER_PREPARING" ->
                log.debug("Order preparing event received, no delivery action needed at this stage");
            default -> log.debug("Ignoring kitchen event type: {}", eventType);
        }
        ack.acknowledge();
    }

    private void handleOrderReady(JsonNode event) {
        try {
            // Kitchen service emits orderId as Long (matching order-service primary key)
            Long orderId = event.get("orderId").asLong();
            String orderNumber = event.path("orderNumber").asText();

            // Check if this is a delivery order
            JsonNode orderTypeNode = event.get("orderType");
            if (orderTypeNode == null || !"DELIVERY".equals(orderTypeNode.asText())) {
                log.debug("Order {} is not for delivery, skipping", orderId);
                return;
            }

            log.info("Order {} ({}) is ready for delivery initiation", orderId, orderNumber);

            // Delivery creation is initiated by the order-service or via REST API once
            // the full delivery details (address, coordinates) are known.
            // This event serves as a trigger signal; the actual CreateDeliveryRequest
            // requires address/coordinates that are not present in the kitchen event.
            log.info("Delivery initiation signal received for order {} ({})", orderId, orderNumber);

        } catch (Exception e) {
            log.error("Failed to handle order.ready event: {}", e.getMessage(), e);
        }
    }
}
