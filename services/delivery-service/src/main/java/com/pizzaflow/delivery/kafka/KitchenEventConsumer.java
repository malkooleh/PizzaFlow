package com.pizzaflow.delivery.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KitchenEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KitchenEventConsumer.class);
    private static final int MAX_LOGGED_PAYLOAD_LENGTH = 500;

    private final ObjectMapper objectMapper;

    public KitchenEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Listen for kitchen events. Only {@code ORDER_READY} triggers delivery
     * initiation.
     * Poison (unparseable) messages are safely skipped with an explicit log entry.
     * Any recoverable exception in business handling propagates — preventing ack so
     * the broker can redeliver and eventually route to the DLT.
     */
    @KafkaListener(topics = { "order.preparing", "order.ready" }, groupId = "delivery-service")
    public void handleKitchenEvent(String message, Acknowledgment ack) {
        JsonNode event;
        try {
            event = objectMapper.readTree(message);
        } catch (Exception e) {
            // Poison message — unparseable JSON will never succeed on retry; skip it.
            log.error("Poisoned kitchen event message (unparseable JSON), skipping permanently. payload='{}'",
                    truncate(message), e);
            ack.acknowledge();
            return;
        }

        String eventType = event.path("eventType").asText();
        if (eventType.isBlank()) {
            throw new IllegalArgumentException("Kitchen event is missing required field: eventType");
        }

        switch (eventType) {
            case "ORDER_READY" -> handleOrderReady(event);
            case "ORDER_PREPARING" ->
                log.debug("Order preparing event received, no delivery action needed at this stage");
            default -> throw new IllegalArgumentException("Unsupported kitchen eventType: " + eventType);
        }

        // Acknowledge only after successful business handling.
        // If handleOrderReady throws, this line is never reached
        // and the broker will redeliver the message.
        ack.acknowledge();
    }

    private void handleOrderReady(JsonNode event) {
        JsonNode orderIdNode = event.path("orderId");
        if (orderIdNode.isMissingNode() || orderIdNode.isNull()) {
            throw new IllegalArgumentException("ORDER_READY event is missing required field: orderId");
        }
        Long orderId = orderIdNode.asLong();
        String orderNumber = event.path("orderNumber").asText();

        String orderType = event.path("orderType").asText();
        if (orderType.isEmpty() || !"DELIVERY".equals(orderType)) {
            log.debug("Order {} is not for delivery (type={}), skipping", orderId, orderType);
            return;
        }

        log.info("Delivery initiation signal received for order {} ({})", orderId, orderNumber);
        // Delivery creation requires full address/coordinates not present in kitchen
        // events.
        // The actual CreateDelivery call is triggered via a REST endpoint once those
        // details
        // are available (e.g., from order-service). This handler records the readiness
        // signal.
    }

    private String truncate(String value) {
        if (value == null) {
            return "null";
        }
        return value.length() <= MAX_LOGGED_PAYLOAD_LENGTH
                ? value
                : value.substring(0, MAX_LOGGED_PAYLOAD_LENGTH) + "...";
    }
}
