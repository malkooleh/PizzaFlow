package com.pizzaflow.delivery.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaflow.delivery.event.DeliveryEvent;
import com.pizzaflow.delivery.model.Courier;
import com.pizzaflow.delivery.model.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DeliveryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventProducer.class);
    private static final String DELIVERY_TOPIC = "delivery-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DeliveryEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendDeliveryCreated(Delivery delivery) {
        sendEvent(DeliveryEvent.DELIVERY_CREATED, delivery, null, null, null);
    }

    public void sendCourierAssigned(Delivery delivery) {
        sendEvent(DeliveryEvent.COURIER_ASSIGNED, delivery, null, null, null);
    }

    public void sendOrderPickedUp(Delivery delivery) {
        sendEvent(DeliveryEvent.ORDER_PICKED_UP, delivery, null, null, null);
    }

    public void sendDeliveryInTransit(Delivery delivery) {
        sendEvent(DeliveryEvent.DELIVERY_IN_TRANSIT, delivery, null, null, null);
    }

    public void sendCourierArrived(Delivery delivery) {
        sendEvent(DeliveryEvent.COURIER_ARRIVED, delivery, null, null, null);
    }

    public void sendDeliveryCompleted(Delivery delivery) {
        sendEvent(DeliveryEvent.DELIVERY_COMPLETED, delivery, null, null, null);
    }

    public void sendDeliveryFailed(Delivery delivery, String reason) {
        sendEvent(DeliveryEvent.DELIVERY_FAILED, delivery, null, null, reason);
    }

    public void sendLocationUpdate(Delivery delivery, BigDecimal latitude, BigDecimal longitude) {
        sendEvent(DeliveryEvent.LOCATION_UPDATED, delivery, latitude, longitude, null);
    }

    private void sendEvent(String eventType, Delivery delivery, BigDecimal latitude, BigDecimal longitude, String failureReason) {
        Courier courier = delivery.getCourier();
        
        DeliveryEvent event = new DeliveryEvent(
            eventType,
            delivery.getId(),
            delivery.getOrderId(),
            courier != null ? courier.getId() : null,
            courier != null ? courier.getName() : null,
            delivery.getCustomerId(),
            delivery.getStatus(),
            delivery.getDeliveryAddress(),
            latitude != null ? latitude : (courier != null ? courier.getCurrentLatitude() : null),
            longitude != null ? longitude : (courier != null ? courier.getCurrentLongitude() : null),
            delivery.getEstimatedDeliveryTime(),
            failureReason,
            LocalDateTime.now()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(DELIVERY_TOPIC, delivery.getId().toString(), payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send {} event for delivery {}: {}",
                            eventType, delivery.getId(), ex.getMessage());
                    } else {
                        log.info("Sent {} event for delivery {} (order: {})",
                            eventType, delivery.getId(), delivery.getOrderId());
                    }
                });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize delivery event: {}", e.getMessage());
        }
    }
}
