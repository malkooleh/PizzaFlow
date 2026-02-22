package com.pizzaflow.kitchen.service;

import com.pizzaflow.kitchen.event.OrderPreparingEvent;
import com.pizzaflow.kitchen.event.OrderReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for publishing kitchen-related events to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_PREPARING_TOPIC = "order.preparing";
    private static final String ORDER_READY_TOPIC = "order.ready";

    public void publishOrderPreparing(Long orderId, String orderNumber, Long restaurantId,
            Integer estimatedPrepTimeMinutes, String assignedStation) {
        OrderPreparingEvent event = OrderPreparingEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("ORDER_PREPARING")
                .timestamp(LocalDateTime.now())
                .source("kitchen-service")
                .version(1)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .restaurantId(restaurantId)
                .estimatedPrepTimeMinutes(estimatedPrepTimeMinutes)
                .assignedStation(assignedStation)
                .build();

        kafkaTemplate.send(ORDER_PREPARING_TOPIC, orderId.toString(), event);
        log.info("Published order.preparing event: orderId={}", orderId);
    }

    public void publishOrderReady(Long orderId, String orderNumber, Long restaurantId,
            Long customerId, String orderType, Integer actualPrepTimeMinutes) {
        OrderReadyEvent event = OrderReadyEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("ORDER_READY")
                .timestamp(LocalDateTime.now())
                .source("kitchen-service")
                .version(1)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .restaurantId(restaurantId)
                .customerId(customerId)
                .orderType(orderType)
                .readyAt(LocalDateTime.now())
                .actualPrepTimeMinutes(actualPrepTimeMinutes)
                .build();

        kafkaTemplate.send(ORDER_READY_TOPIC, orderId.toString(), event);
        log.info("Published order.ready event: orderId={}", orderId);
    }
}
