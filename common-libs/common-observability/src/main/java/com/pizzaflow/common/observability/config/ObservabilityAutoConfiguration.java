package com.pizzaflow.common.observability.config;

import com.pizzaflow.common.observability.kafka.KafkaTracingUtils;
import com.pizzaflow.common.observability.metrics.BusinessMetrics;
import com.pizzaflow.common.observability.tracing.TracingUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for PizzaFlow observability components.
 * 
 * <p>Automatically configures:
 * <ul>
 *   <li>Distributed tracing with @Observed annotation support</li>
 *   <li>Metrics collection with @Timed annotation support</li>
 *   <li>MDC logging correlation with trace IDs</li>
 *   <li>Business-specific metrics recording</li>
 *   <li>Kafka trace context propagation (when Kafka is available)</li>
 * </ul>
 */
@AutoConfiguration
@Import({
    TracingConfig.class,
    MetricsConfig.class,
    LoggingConfig.class
})
public class ObservabilityAutoConfiguration {

    /**
     * Creates TracingUtils for programmatic span management.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({Tracer.class, ObservationRegistry.class})
    public TracingUtils tracingUtils(Tracer tracer, ObservationRegistry observationRegistry) {
        return new TracingUtils(tracer, observationRegistry);
    }

    /**
     * Creates BusinessMetrics for recording pizza-specific business metrics.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry.class)
    public BusinessMetrics businessMetrics(MeterRegistry meterRegistry) {
        return new BusinessMetrics(meterRegistry);
    }

    /**
     * Creates KafkaTracingUtils for Kafka trace context propagation.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(Tracer.class)
    @ConditionalOnClass(ConsumerRecord.class)
    public KafkaTracingUtils kafkaTracingUtils(Tracer tracer) {
        return new KafkaTracingUtils(tracer);
    }
}
