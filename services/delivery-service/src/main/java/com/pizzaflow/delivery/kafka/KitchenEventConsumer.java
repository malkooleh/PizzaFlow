package com.pizzaflow.delivery.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaflow.delivery.dto.CreateDeliveryRequest;
import com.pizzaflow.delivery.model.enums.DeliveryPriority;
import com.pizzaflow.delivery.service.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

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
    @KafkaListener(topics = "kitchen-events", groupId = "delivery-service")
    public void handleKitchenEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.get("eventType").asText();

            if ("order.ready".equals(eventType)) {
                handleOrderReady(event);
            }
        } catch (Exception e) {
            log.error("Failed to process kitchen event: {}", e.getMessage(), e);
        }
    }

    private void handleOrderReady(JsonNode event) {
        try {
            UUID orderId = UUID.fromString(event.get("orderId").asText());
            
            // Check if this is a delivery order
            JsonNode orderTypeNode = event.get("orderType");
            if (orderTypeNode == null || !"DELIVERY".equals(orderTypeNode.asText())) {
                log.debug("Order {} is not for delivery, skipping", orderId);
                return;
            }

            log.info("Order {} is ready for delivery", orderId);

            // In a real scenario, we'd get customer/delivery details from Order Service
            // For now, we'll log that delivery should be initiated
            // The Order Service should send a more complete event or we should call it
            
            log.info("Delivery initiation triggered for order {}", orderId);
            
        } catch (Exception e) {
            log.error("Failed to handle order.ready event: {}", e.getMessage(), e);
        }
    }
}
