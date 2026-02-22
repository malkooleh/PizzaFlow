package com.pizzaflow.common.grpc.config;

import io.grpc.ClientInterceptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC Client Configuration for Pizza Flow services.
 * 
 * Provides common interceptors and configuration for gRPC clients.
 */
@Configuration
public class GrpcClientConfig {

    /**
     * Creates a global logging interceptor for all gRPC client calls.
     */
    @Bean
    @GrpcGlobalClientInterceptor
    public ClientInterceptor clientLoggingInterceptor() {
        return new LoggingClientInterceptor();
    }

    /**
     * Creates a global metrics interceptor for all gRPC client calls.
     */
    @Bean
    @GrpcGlobalClientInterceptor
    public ClientInterceptor clientMetricsInterceptor() {
        return new MetricsClientInterceptor();
    }

    /**
     * Creates a global retry interceptor for all gRPC client calls.
     */
    @Bean
    @GrpcGlobalClientInterceptor
    public ClientInterceptor retryInterceptor() {
        return new RetryClientInterceptor();
    }
}
