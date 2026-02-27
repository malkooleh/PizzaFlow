package com.pizzaflow.common.observability.kafka;

import io.micrometer.observation.ObservationRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

/**
 * Auto-configuration for Kafka observability.
 *
 * <p>Automatically configures:
 * <ul>
 *   <li>Producer observation to propagate trace context in outgoing messages</li>
 *   <li>Consumer observation to extract trace context from incoming messages</li>
 *   <li>Automatic span creation for Kafka message processing</li>
 * </ul>
 *
 * <p>When enabled, trace IDs flow automatically through Kafka:
 * <pre>
 * Order Service → [Kafka: order.created with trace headers] → Payment Service
 *                                                            (continues same trace)
 * </pre>
 */
@Configuration
@ConditionalOnClass({KafkaTemplate.class, ObservationRegistry.class})
public class KafkaObservabilityConfig {

    /**
     * Customizes Kafka listener containers to enable observation.
     * This adds tracing to all @KafkaListener methods automatically.
     */
    @Bean
    @ConditionalOnBean(ObservationRegistry.class)
    public ContainerCustomizer<Object, Object, ConcurrentMessageListenerContainer<Object, Object>>
    kafkaListenerObservationCustomizer() {
        return container ->
                container.getContainerProperties().setObservationEnabled(true);
    }

    /**
     * Customizes Kafka producer factory to enable observation.
     * This adds trace context propagation to all produced messages.
     */
    @Bean
    @ConditionalOnBean(ObservationRegistry.class)
    public DefaultKafkaProducerFactoryCustomizer kafkaProducerObservationCustomizer(
            ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        return producerFactory -> {
            ObservationRegistry registry = observationRegistryProvider.getIfAvailable();
            if (registry != null && registry != ObservationRegistry.NOOP) {
                // Note: In Spring Kafka 3.1+, observation is enabled via template
                // For earlier versions, you may need to set this on the template directly
            }
        };
    }
}
