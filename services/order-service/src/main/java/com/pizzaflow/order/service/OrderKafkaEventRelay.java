package com.pizzaflow.order.service;

import com.pizzaflow.order.event.OrderCreatedEvent;
import com.pizzaflow.order.event.internal.OrderCreatedApplicationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Relays order domain events to Kafka after the DB transaction has committed.
 *
 * <p>
 * Using {@code AFTER_COMMIT} guarantees that Kafka messages are never emitted
 * for transactions that were ultimately rolled back — eliminating phantom
 * events
 * where downstream services (payment, notification) react to data that never
 * persisted.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaEventRelay {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_CREATED_TOPIC = "order.created";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedApplicationEvent event) {
        log.debug("Relaying order.created to Kafka for order: {}", event.orderId());

        OrderCreatedEvent kafkaEvent = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("ORDER_CREATED")
                .timestamp(LocalDateTime.now())
                .source("order-service")
                .version(1)
                .orderId(event.orderId())
                .orderNumber(event.orderNumber())
                .customerId(event.customerId())
                .restaurantId(event.restaurantId())
                .totalAmount(event.totalAmount())
                .orderType(event.orderType())
                .build();

        kafkaTemplate.send(ORDER_CREATED_TOPIC, event.orderId().toString(), kafkaEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order.created to Kafka for order '{}': {}",
                                event.orderNumber(), ex.getMessage(), ex);
                    } else {
                        log.info("Published order.created event for order: {}", event.orderNumber());
                    }
                });
    }
}
