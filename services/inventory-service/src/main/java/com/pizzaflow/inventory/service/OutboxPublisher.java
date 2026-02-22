package com.pizzaflow.inventory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaflow.inventory.model.OutboxEvent;
import com.pizzaflow.inventory.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service that publishes outbox events to Kafka.
 * Implements the Transactional Outbox pattern for reliable event publishing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 50;

    /**
     * Scheduled job to poll unpublished events and send them to Kafka.
     * Runs every 5 seconds.
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findAndLockUnpublishedEvents(BATCH_SIZE);

        if (events.isEmpty()) {
            return;
        }

        log.debug("Found {} unpublished events to process", events.size());

        for (OutboxEvent event : events) {
            try {
                String topic = determineTopicForEvent(event.getEventType());
                boolean success = kafkaProducerService.sendMessageSync(
                        topic,
                        event.getAggregateId(),
                        event.getPayload());

                if (success) {
                    outboxRepository.markAsPublished(event.getId(), LocalDateTime.now());
                    log.info("Published outbox event: id={}, type={}, topic={}",
                            event.getId(), event.getEventType(), topic);
                } else {
                    log.warn("Failed to publish outbox event: id={}, type={}",
                            event.getId(), event.getEventType());
                }
            } catch (Exception e) {
                log.error("Error publishing outbox event: id={}, error={}",
                        event.getId(), e.getMessage());
            }
        }
    }

    /**
     * Cleanup old published events to prevent table growth.
     * Runs every hour.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupPublishedEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        int deleted = outboxRepository.deletePublishedEventsBefore(threshold);

        if (deleted > 0) {
            log.info("Cleaned up {} old published events", deleted);
        }
    }

    private String determineTopicForEvent(String eventType) {
        return switch (eventType) {
            case "INVENTORY_RESERVED" -> "inventory.reserved";
            case "INVENTORY_RESERVATION_FAILED" -> "inventory.reservation.failed";
            case "INVENTORY_CONSUMED" -> "inventory.consumed";
            case "INVENTORY_RELEASED" -> "inventory.released";
            case "LOW_STOCK_ALERT" -> "inventory.low-stock.alert";
            default -> "inventory.events";
        };
    }
}
