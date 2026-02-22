package com.pizzaflow.common.resilience.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Common Resilience4j configuration for all PizzaFlow services.
 * Provides pre-configured instances for Circuit Breaker, Retry, Rate Limiter, etc.
 */
@Configuration
public class ResilienceConfig {

    // ============================================================================
    // Circuit Breaker Registry
    // ============================================================================
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        // Default configuration for all circuit breakers
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(IOException.class, TimeoutException.class, RuntimeException.class)
                .build();

        // Catalog Service - more lenient (read operations)
        CircuitBreakerConfig catalogConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();

        // Inventory Service - strict (critical for order processing)
        CircuitBreakerConfig inventoryConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(40)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        // Payment Service - very strict (financial operations)
        CircuitBreakerConfig paymentConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .failureRateThreshold(30)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slowCallDurationThreshold(Duration.ofSeconds(3))
                .permittedNumberOfCallsInHalfOpenState(1)
                .build();

        return CircuitBreakerRegistry.of(defaultConfig)
                .addConfiguration("catalogService", catalogConfig)
                .addConfiguration("inventoryService", inventoryConfig)
                .addConfiguration("paymentService", paymentConfig);
    }

    // ============================================================================
    // Retry Registry
    // ============================================================================
    
    @Bean
    public RetryRegistry retryRegistry() {
        // Default retry configuration
        RetryConfig defaultConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .exponentialBackoffMultiplier(2.0)
                .retryExceptions(IOException.class, TimeoutException.class)
                .build();

        // Backend services retry (more aggressive for read operations)
        RetryConfig backendConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(300))
                .exponentialBackoffMultiplier(1.5)
                .build();

        // Payment retry (conservative - avoid duplicate charges)
        RetryConfig paymentRetryConfig = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofSeconds(1))
                .retryOnResult(response -> response == null)
                .build();

        return RetryRegistry.of(defaultConfig)
                .addConfiguration("backendServices", backendConfig)
                .addConfiguration("paymentService", paymentRetryConfig);
    }

    // ============================================================================
    // Rate Limiter Registry
    // ============================================================================
    
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        // Default rate limiter
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofMillis(500))
                .build();

        // Per-user rate limiter (for API Gateway)
        RateLimiterConfig perUserConfig = RateLimiterConfig.custom()
                .limitForPeriod(50)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofMillis(100))
                .build();

        // High-throughput endpoints (menu browsing)
        RateLimiterConfig highThroughputConfig = RateLimiterConfig.custom()
                .limitForPeriod(500)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        // Strict rate limiter (payment, booking)
        RateLimiterConfig strictConfig = RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofMillis(200))
                .build();

        return RateLimiterRegistry.of(defaultConfig)
                .addConfiguration("perUser", perUserConfig)
                .addConfiguration("highThroughput", highThroughputConfig)
                .addConfiguration("strict", strictConfig);
    }

    // ============================================================================
    // Time Limiter Registry
    // ============================================================================
    
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig defaultConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .cancelRunningFuture(true)
                .build();

        // External service calls (longer timeout)
        TimeLimiterConfig externalConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();

        // Fast operations (cache lookups)
        TimeLimiterConfig fastConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(500))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiterRegistry.of(defaultConfig)
                .addConfiguration("externalService", externalConfig)
                .addConfiguration("fastOperation", fastConfig);
    }

    // ============================================================================
    // Bulkhead Registry
    // ============================================================================
    
    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        // Default bulkhead (semaphore-based for Virtual Threads)
        BulkheadConfig defaultConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(25)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();

        // Payment service bulkhead (isolated, lower concurrency)
        BulkheadConfig paymentConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();

        // High-concurrency bulkhead (catalog browsing)
        BulkheadConfig highConcurrencyConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(50)
                .maxWaitDuration(Duration.ofMillis(200))
                .build();

        return BulkheadRegistry.of(defaultConfig)
                .addConfiguration("paymentService", paymentConfig)
                .addConfiguration("highConcurrency", highConcurrencyConfig);
    }
}
