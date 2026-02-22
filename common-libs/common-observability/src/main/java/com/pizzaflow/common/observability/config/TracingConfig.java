package com.pizzaflow.common.observability.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for distributed tracing using Micrometer Tracing.
 * 
 * Supports both Zipkin (Brave) and OpenTelemetry backends.
 */
@Configuration
@ConditionalOnClass(Tracer.class)
@AutoConfigureAfter(ObservationAutoConfiguration.class)
public class TracingConfig {

    /**
     * Creates an ObservedAspect for @Observed annotation support.
     * This enables automatic tracing of methods annotated with @Observed.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ObservationRegistry.class)
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
