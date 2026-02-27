package com.pizzaflow.order.eventsourcing.service;

import com.pizzaflow.order.eventsourcing.aggregate.OrderAggregate;
import com.pizzaflow.order.eventsourcing.aggregate.OrderAggregateRepository;
import com.pizzaflow.order.eventsourcing.aggregate.PlaceOrderCommand;
import com.pizzaflow.order.eventsourcing.event.OrderDomainEvent;
import com.pizzaflow.order.eventsourcing.readmodel.OrderReadModelProjection;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Command service for order operations (CQRS write side).
 * Handles all commands that modify order state through the aggregate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Observed(name = "order.commands", contextualName = "order-command-service")
public class OrderCommandService {

    private final OrderAggregateRepository aggregateRepository;
    private final OrderReadModelProjection readModelProjection;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_EVENTS_TOPIC = "order.events";

    /**
     * Place a new order.
     */
    @Transactional
    @Observed(name = "order.place", contextualName = "place-order")
    public UUID placeOrder(PlaceOrderCommand command) {
        log.info("Processing PlaceOrder command for customer: {}", command.getCustomerId());

        OrderAggregate aggregate = aggregateRepository.create();
        aggregate.placeOrder(command);

        aggregateRepository.save(aggregate);

        // Project events to read model and publish to Kafka
        processUncommittedEvents(aggregate);

        log.info("Order placed successfully: {}", aggregate.getOrderNumber());
        return aggregate.getId();
    }

    /**
     * Confirm an order after payment.
     */
    @Transactional
    @Observed(name = "order.confirm", contextualName = "confirm-order")
    public void confirmOrder(UUID orderId, String paymentReference, Integer estimatedPrepTime, String triggeredBy) {
        log.info("Processing ConfirmOrder command for order: {}", orderId);

        OrderAggregate aggregate = aggregateRepository.getById(orderId);
        aggregate.confirmOrder(paymentReference, estimatedPrepTime, triggeredBy);

        aggregateRepository.save(aggregate);
        processUncommittedEvents(aggregate);

        log.info("Order confirmed: {}", orderId);
    }

    /**
     * Start preparing an order.
     */
    @Transactional
    public void startPreparing(UUID orderId, String kitchenStation, String assignedStaff,
            Instant estimatedCompletion, String triggeredBy) {
        log.info("Processing StartPreparing command for order: {}", orderId);

        OrderAggregate aggregate = aggregateRepository.getById(orderId);
        aggregate.startPreparing(kitchenStation, assignedStaff, estimatedCompletion, triggeredBy);

        aggregateRepository.save(aggregate);
        processUncommittedEvents(aggregate);

        log.info("Order preparation started: {}", orderId);
    }

    /**
     * Mark order as ready.
     */
    @Transactional
    public void markOrderReady(UUID orderId, String pickupLocation, String packagingNotes, String triggeredBy) {
        log.info("Processing MarkReady command for order: {}", orderId);

        OrderAggregate aggregate = aggregateRepository.getById(orderId);
        aggregate.markReady(pickupLocation, packagingNotes, triggeredBy);

        aggregateRepository.save(aggregate);
        processUncommittedEvents(aggregate);

        log.info("Order marked ready: {}", orderId);
    }

    /**
     * Record order pickup by courier.
     */
    @Transactional
    public void pickUpOrder(UUID orderId, UUID courierId, String courierName,
            Instant estimatedDelivery, String triggeredBy) {
        log.info("Processing PickUp command for order: {}", orderId);

        OrderAggregate aggregate = aggregateRepository.getById(orderId);
        aggregate.pickUp(courierId, courierName, estimatedDelivery, triggeredBy);

        aggregateRepository.save(aggregate);
        processUncommittedEvents(aggregate);

        log.info("Order picked up: {}", orderId);
    }

    /**
     * Mark order as delivered.
     */
    @Transactional
    public void deliverOrder(UUID orderId, String receivedBy, String confirmation,
            String notes, String triggeredBy) {
        log.info("Processing Deliver command for order: {}", orderId);

        OrderAggregate aggregate = aggregateRepository.getById(orderId);
        aggregate.deliver(receivedBy, confirmation, notes, triggeredBy);

        aggregateRepository.save(aggregate);
        processUncommittedEvents(aggregate);

        log.info("Order delivered: {}", orderId);
    }

    /**
     * Complete an order.
     */
    @Transactional
    public void completeOrder(UUID orderId, Integer rating, String feedback, String triggeredBy) {
        log.info("Processing Complete command for order: {}", orderId);

        OrderAggregate aggregate = aggregateRepository.getById(orderId);
        aggregate.complete(rating, feedback, triggeredBy);

        aggregateRepository.save(aggregate);
        processUncommittedEvents(aggregate);

        log.info("Order completed: {}", orderId);
    }

    /**
     * Cancel an order.
     */
    @Transactional
    public void cancelOrder(UUID orderId, String reason, String cancelledBy,
            BigDecimal refundAmount, String triggeredBy) {
        log.info("Processing Cancel command for order: {}", orderId);

        OrderAggregate aggregate = aggregateRepository.getById(orderId);
        aggregate.cancel(reason, cancelledBy, refundAmount, triggeredBy);

        aggregateRepository.save(aggregate);
        processUncommittedEvents(aggregate);

        log.info("Order cancelled: {}", orderId);
    }

    /**
     * Add an item to an order.
     */
    @Transactional
    public void addItem(UUID orderId, String itemId, String itemName, Integer quantity,
            BigDecimal unitPrice, String instructions, String triggeredBy) {
        log.info("Processing AddItem command for order: {}", orderId);

        OrderAggregate aggregate = aggregateRepository.getById(orderId);
        aggregate.addItem(itemId, itemName, quantity, unitPrice, instructions, triggeredBy);

        aggregateRepository.save(aggregate);
        processUncommittedEvents(aggregate);

        log.info("Item added to order: {}", orderId);
    }

    /**
     * Remove an item from an order.
     */
    @Transactional
    public void removeItem(UUID orderId, UUID itemId, String reason, String triggeredBy) {
        log.info("Processing RemoveItem command for order: {}", orderId);

        OrderAggregate aggregate = aggregateRepository.getById(orderId);
        aggregate.removeItem(itemId, reason, triggeredBy);

        aggregateRepository.save(aggregate);
        processUncommittedEvents(aggregate);

        log.info("Item removed from order: {}", orderId);
    }

    /**
     * Process uncommitted events: project to read model and publish to Kafka.
     */
    private void processUncommittedEvents(OrderAggregate aggregate) {
        List<OrderDomainEvent> events = aggregate.getUncommittedEvents();

        for (OrderDomainEvent event : events) {
            // Project to read model
            readModelProjection.project(event);

            // Publish to Kafka for other services
            kafkaTemplate.send(ORDER_EVENTS_TOPIC, aggregate.getId().toString(), event);
            log.debug("Published event {} to Kafka", event.getEventType());
        }
    }
}
