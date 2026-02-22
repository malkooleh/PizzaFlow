package com.pizzaflow.payment.listener;

import com.pizzaflow.payment.event.OrderCreatedEvent;
import com.pizzaflow.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for order-related events.
 * Listens to order.created events and initiates automatic payment processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final PaymentService paymentService;

    /**
     * Listen for order.created events and initiate payment processing.
     * This automatically starts payment flow when a new order is placed.
     */
    @KafkaListener(topics = "order.created", groupId = "payment-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderCreated(@Payload OrderCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received order.created event: orderId={}, orderNumber={}, amount={}",
                event.getOrderId(), event.getOrderNumber(), event.getTotalAmount());

        try {
            // Process payment for the order
            // In a real system, you might check order type first (e.g., skip for
            // CASH_ON_DELIVERY)
            paymentService.processOrderPayment(
                    event.getOrderId(),
                    event.getCustomerId(),
                    event.getTotalAmount());

            acknowledgment.acknowledge();
            log.info("Successfully processed payment for order: {}", event.getOrderId());

        } catch (IllegalStateException e) {
            // Payment already exists - acknowledge and skip
            log.warn("Payment already exists for order: {}. Skipping.", event.getOrderId());
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing payment for order: {}", event.getOrderId(), e);
            // Don't acknowledge - message will be redelivered
            // In production, implement retry logic or send to DLQ
            throw e;
        }
    }
}
