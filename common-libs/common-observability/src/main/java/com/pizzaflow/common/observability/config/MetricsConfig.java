package com.pizzaflow.common.observability.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration for metrics collection using Micrometer.
 */
@Configuration
public class MetricsConfig {

    /**
     * Customizes the MeterRegistry with common tags for all metrics.
     */
    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> registry.config().commonTags(Tags.of(
                "application", environment.getProperty("spring.application.name", "unknown"),
                "env", environment.getProperty("spring.profiles.active", "default")
        ));
    }

    /**
     * Creates a TimedAspect for @Timed annotation support.
     * This enables automatic timing of methods annotated with @Timed.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry.class)
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
