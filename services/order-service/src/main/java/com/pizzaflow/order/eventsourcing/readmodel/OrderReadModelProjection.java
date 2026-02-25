package com.pizzaflow.order.eventsourcing.readmodel;

import com.pizzaflow.order.eventsourcing.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Event projection handler that updates the read model based on domain events.
 * This is the core of CQRS - projecting events to a denormalized read model.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReadModelProjection {

    private final OrderReadModelRepository orderReadModelRepository;

    /**
     * Project a domain event to the read model.
     */
    @Transactional
    public void project(OrderDomainEvent event) {
        log.debug("Projecting event {} for aggregate {}", event.getEventType(), event.getAggregateId());

        switch (event) {
            case OrderPlacedEvent e -> projectOrderPlaced(e);
            case OrderConfirmedEvent e -> projectOrderConfirmed(e);
            case OrderPreparingEvent e -> projectOrderPreparing(e);
            case OrderReadyEvent e -> projectOrderReady(e);
            case OrderPickedUpEvent e -> projectOrderPickedUp(e);
            case OrderDeliveredEvent e -> projectOrderDelivered(e);
            case OrderCompletedEvent e -> projectOrderCompleted(e);
            case OrderCancelledEvent e -> projectOrderCancelled(e);
            case OrderItemAddedEvent e -> projectOrderItemAdded(e);
            case OrderItemRemovedEvent e -> projectOrderItemRemoved(e);
            default -> log.warn("Unknown event type for projection: {}", event.getClass().getSimpleName());
        }
    }

    private void projectOrderPlaced(OrderPlacedEvent event) {
        OrderReadModel readModel = OrderReadModel.builder()
                .id(event.getAggregateId())
                .orderNumber(event.getOrderNumber())
                .customerId(event.getCustomerId())
                .restaurantId(event.getRestaurantId())
                .orderType(event.getOrderType())
                .status("PENDING")
                .subtotal(event.getSubtotal())
                .tax(event.getTax())
                .deliveryFee(event.getDeliveryFee())
                .totalAmount(event.getTotalAmount())
                .deliveryAddressStreet(event.getDeliveryAddressStreet())
                .deliveryAddressCity(event.getDeliveryAddressCity())
                .deliveryAddressPostalCode(event.getDeliveryAddressPostalCode())
                .deliveryAddressLatitude(event.getDeliveryAddressLatitude())
                .deliveryAddressLongitude(event.getDeliveryAddressLongitude())
                .scheduledTime(event.getScheduledTime() != null
                        ? event.getScheduledTime().toInstant(ZoneOffset.UTC)
                        : null)
                .createdAt(event.getTimestamp())
                .lastEventId(event.getEventId())
                .lastUpdatedAt(Instant.now())
                .version(0L)
                .build();

        // Add order items
        if (event.getItems() != null) {
            for (OrderPlacedEvent.OrderItemData itemData : event.getItems()) {
                OrderItemReadModel item = OrderItemReadModel.builder()
                        .id(itemData.getItemId())
                        .itemId(itemData.getItemId().toString())
                        .itemName(itemData.getItemName())
                        .quantity(itemData.getQuantity())
                        .unitPrice(itemData.getUnitPrice())
                        .totalPrice(itemData.getTotalPrice())
                        .specialInstructions(itemData.getSpecialInstructions())
                        .build();
                readModel.addItem(item);
            }
        }

        orderReadModelRepository.save(readModel);
        log.debug("Created read model for order {}", event.getOrderNumber());
    }

    private void projectOrderConfirmed(OrderConfirmedEvent event) {
        OrderReadModel readModel = getReadModel(event.getAggregateId());
        readModel.setStatus("CONFIRMED");
        readModel.setConfirmedAt(event.getConfirmedAt());
        readModel.setLastEventId(event.getEventId());
        orderReadModelRepository.save(readModel);
    }

    private void projectOrderPreparing(OrderPreparingEvent event) {
        OrderReadModel readModel = getReadModel(event.getAggregateId());
        readModel.setStatus("PREPARING");
        readModel.setPreparingAt(event.getPreparationStartedAt());
        readModel.setLastEventId(event.getEventId());
        orderReadModelRepository.save(readModel);
    }

    private void projectOrderReady(OrderReadyEvent event) {
        OrderReadModel readModel = getReadModel(event.getAggregateId());
        readModel.setStatus("READY");
        readModel.setReadyAt(event.getReadyAt());
        readModel.setLastEventId(event.getEventId());
        orderReadModelRepository.save(readModel);
    }

    private void projectOrderPickedUp(OrderPickedUpEvent event) {
        OrderReadModel readModel = getReadModel(event.getAggregateId());
        readModel.setStatus("OUT_FOR_DELIVERY");
        readModel.setPickedUpAt(event.getPickedUpAt());
        readModel.setCourierId(event.getCourierId());
        readModel.setCourierName(event.getCourierName());
        readModel.setEstimatedDeliveryTime(event.getEstimatedDeliveryTime());
        readModel.setLastEventId(event.getEventId());
        orderReadModelRepository.save(readModel);
    }

    private void projectOrderDelivered(OrderDeliveredEvent event) {
        OrderReadModel readModel = getReadModel(event.getAggregateId());
        readModel.setStatus("DELIVERED");
        readModel.setDeliveredAt(event.getDeliveredAt());
        readModel.setLastEventId(event.getEventId());
        orderReadModelRepository.save(readModel);
    }

    private void projectOrderCompleted(OrderCompletedEvent event) {
        OrderReadModel readModel = getReadModel(event.getAggregateId());
        readModel.setStatus("COMPLETED");
        readModel.setCompletedAt(event.getCompletedAt());
        readModel.setLastEventId(event.getEventId());
        orderReadModelRepository.save(readModel);
    }

    private void projectOrderCancelled(OrderCancelledEvent event) {
        OrderReadModel readModel = getReadModel(event.getAggregateId());
        readModel.setStatus("CANCELLED");
        readModel.setCancelledAt(event.getCancelledAt());
        readModel.setCancellationReason(event.getCancellationReason());
        readModel.setCancelledBy(event.getCancelledBy());
        readModel.setLastEventId(event.getEventId());
        orderReadModelRepository.save(readModel);
    }

    private void projectOrderItemAdded(OrderItemAddedEvent event) {
        OrderReadModel readModel = orderReadModelRepository.findByIdWithItems(event.getAggregateId())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + event.getAggregateId()));

        OrderItemReadModel item = OrderItemReadModel.builder()
                .id(event.getOrderItemId())
                .itemId(event.getItemId())
                .itemName(event.getItemName())
                .quantity(event.getQuantity())
                .unitPrice(event.getUnitPrice())
                .totalPrice(event.getTotalPrice())
                .specialInstructions(event.getSpecialInstructions())
                .build();

        readModel.addItem(item);
        readModel.setSubtotal(event.getNewSubtotal());
        readModel.setTax(event.getNewSubtotal().multiply(new java.math.BigDecimal("0.08")));
        readModel.setTotalAmount(event.getNewTotal());
        readModel.setLastEventId(event.getEventId());

        orderReadModelRepository.save(readModel);
    }

    private void projectOrderItemRemoved(OrderItemRemovedEvent event) {
        OrderReadModel readModel = orderReadModelRepository.findByIdWithItems(event.getAggregateId())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + event.getAggregateId()));

        readModel.getItems().removeIf(item -> item.getId().equals(event.getOrderItemId()));
        readModel.setSubtotal(event.getNewSubtotal());
        readModel.setTax(event.getNewSubtotal().multiply(new java.math.BigDecimal("0.08")));
        readModel.setTotalAmount(event.getNewTotal());
        readModel.setLastEventId(event.getEventId());

        orderReadModelRepository.save(readModel);
    }

    private OrderReadModel getReadModel(UUID aggregateId) {
        return orderReadModelRepository.findById(aggregateId)
                .orElseThrow(() -> new IllegalStateException("Order not found in read model: " + aggregateId));
    }

    /**
     * Rebuild the entire read model from events (for data recovery or migration).
     */
    @Transactional
    public void rebuild(UUID aggregateId, java.util.List<OrderDomainEvent> events) {
        // Delete existing read model if present
        orderReadModelRepository.deleteById(aggregateId);

        // Replay all events
        for (OrderDomainEvent event : events) {
            project(event);
        }

        log.info("Rebuilt read model for aggregate {} from {} events", aggregateId, events.size());
    }
}
