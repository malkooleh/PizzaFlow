package com.pizzaflow.common.grpc.config;

import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC Server Configuration for Pizza Flow services.
 * 
 * Provides common interceptors and configuration for gRPC servers.
 */
@Configuration
public class GrpcServerConfig {

    /**
     * Creates a global logging interceptor for all gRPC services.
     */
    @Bean
    @GrpcGlobalServerInterceptor
    public ServerInterceptor loggingInterceptor() {
        return new LoggingServerInterceptor();
    }

    /**
     * Creates a global metrics interceptor for all gRPC services.
     */
    @Bean
    @GrpcGlobalServerInterceptor
    public ServerInterceptor metricsInterceptor() {
        return new MetricsServerInterceptor();
    }

    /**
     * Creates a global error handling interceptor.
     */
    @Bean
    @GrpcGlobalServerInterceptor
    public ServerInterceptor errorHandlingInterceptor() {
        return new ErrorHandlingServerInterceptor();
    }
}
