package com.pizzaflow.kitchen.listener;

import com.pizzaflow.kitchen.event.PaymentCompletedEvent;
import com.pizzaflow.kitchen.service.KitchenQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for payment events.
 * Listens to payment.completed events and adds orders to kitchen queue.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final KitchenQueueService kitchenQueueService;

    /**
     * Listen for payment.completed events and add order to kitchen queue.
     */
    @KafkaListener(topics = "payment.completed", groupId = "kitchen-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentCompleted(@Payload PaymentCompletedEvent event, Acknowledgment acknowledgment) {
        log.info("Received payment.completed event: orderId={}, amount={}",
                event.getOrderId(), event.getAmount());

        try {
            // Add order to kitchen queue
            kitchenQueueService.addOrderToQueue(event);

            acknowledgment.acknowledge();
            log.info("Successfully added order {} to kitchen queue", event.getOrderId());

        } catch (Exception e) {
            log.error("Error adding order to kitchen queue: {}", event.getOrderId(), e);
            // Don't acknowledge - message will be redelivered
            throw e;
        }
    }
}
