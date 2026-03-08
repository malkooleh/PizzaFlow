package com.pizzaflow.inventory.service;

import com.pizzaflow.inventory.model.OutboxEvent;
import com.pizzaflow.inventory.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Publishes outbox events to Kafka using the Transactional Outbox pattern.
 *
 * <p>
 * The scheduling method is intentionally <em>not</em> annotated with
 * {@code @Transactional}: keeping a database transaction open while waiting
 * for a Kafka broker acknowledgement causes prolonged connection-pool locks
 * that degrade throughput under broker slowdown.
 *
 * <p>
 * Instead, DB operations are wrapped in short, focused
 * {@link TransactionTemplate} scopes:
 * <ol>
 * <li>Fetch-and-lock phase — short read transaction (FOR UPDATE SKIP
 * LOCKED).</li>
 * <li>Publish phase — outside any DB transaction.</li>
 * <li>Mark-published phase — minimal write transaction per event.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaProducerService kafkaProducerService;
    private final PlatformTransactionManager transactionManager;

    private static final int BATCH_SIZE = 50;

    /**
     * Scheduled job to poll unpublished events and send them to Kafka.
     * Runs every 5 seconds.
     */
    @Scheduled(fixedRate = 5000)
    public void publishPendingEvents() {
        // Phase 1: fetch with short DB transaction (lock released right after return).
        // FOR UPDATE SKIP LOCKED prevents concurrent scheduler nodes from
        // double-processing.
        List<OutboxEvent> events = new TransactionTemplate(transactionManager)
                .execute(status -> outboxRepository.findAndLockUnpublishedEvents(BATCH_SIZE));

        if (events == null || events.isEmpty()) {
            return;
        }

        log.debug("Found {} unpublished outbox events to process", events.size());

        // Phase 2 & 3: publish outside transaction, then mark in a separate short
        // transaction.
        for (OutboxEvent event : events) {
            publishAsync(event);
        }
    }

    private void publishAsync(OutboxEvent event) {
        String topic = determineTopicForEvent(event.getEventType());

        // Non-blocking send — broker round-trip does NOT hold a DB connection.
        kafkaProducerService.sendMessage(topic, event.getAggregateId(), event.getPayload())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish outbox event id={}, type={}, will retry on next poll: {}",
                                event.getId(), event.getEventType(), ex.getMessage());
                        return;
                    }
                    // Phase 3: minimal write transaction to mark the event as published.
                    new TransactionTemplate(transactionManager).executeWithoutResult(
                            status -> outboxRepository.markAsPublished(event.getId(), LocalDateTime.now()));
                    log.info("Published outbox event: id={}, type={}, topic={}",
                            event.getId(), event.getEventType(), topic);
                });
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
            log.info("Cleaned up {} old published outbox events", deleted);
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
