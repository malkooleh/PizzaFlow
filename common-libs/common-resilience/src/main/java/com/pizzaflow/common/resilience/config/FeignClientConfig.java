package com.pizzaflow.common.resilience.config;

import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign client configuration with Resilience4j integration.
 */
@Configuration
@EnableFeignClients(basePackages = "com.pizzaflow")
public class FeignClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Retryer retryer() {
        // Disable Feign's built-in retry (use Resilience4j instead)
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * Creates a Feign.Builder with Resilience4j decorators.
     */
    @Bean
    public Resilience4jFeign.Builder resilience4jFeignBuilder(
            CircuitBreakerRegistry circuitBreakerRegistry) {
        
        FeignDecorators decorators = FeignDecorators.builder()
                .withCircuitBreaker(circuitBreakerRegistry.circuitBreaker("default"))
                .build();
        
        return Resilience4jFeign.builder(decorators);
    }
}
