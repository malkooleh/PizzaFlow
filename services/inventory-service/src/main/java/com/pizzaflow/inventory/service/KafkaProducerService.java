package com.pizzaflow.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Send a message to the specified Kafka topic.
     * 
     * @param topic   The topic to send the message to
     * @param key     The message key (used for partitioning)
     * @param payload The JSON payload
     * @return CompletableFuture for async result handling
     */
    public CompletableFuture<SendResult<String, String>> sendMessage(String topic, String key, String payload) {
        log.debug("Sending message to topic: {}, key: {}", topic, key);

        return kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send message to topic: {}, key: {}, error: {}",
                                topic, key, ex.getMessage());
                    } else {
                        log.info("Message sent to topic: {}, partition: {}, offset: {}",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Send a message synchronously, waiting for acknowledgment.
     * 
     * @param topic   The topic to send the message to
     * @param key     The message key
     * @param payload The JSON payload
     * @return true if sending was successful
     */
    public boolean sendMessageSync(String topic, String key, String payload) {
        try {
            kafkaTemplate.send(topic, key, payload).get();
            log.info("Message sent synchronously to topic: {}, key: {}", topic, key);
            return true;
        } catch (Exception e) {
            log.error("Failed to send message synchronously to topic: {}, key: {}, error: {}",
                    topic, key, e.getMessage());
            return false;
        }
    }
}
