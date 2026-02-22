package com.pizzaflow.common.observability.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Configuration for logging with trace correlation.
 * Adds trace and span IDs to the MDC for logging correlation.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class LoggingConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnClass(Tracer.class)
    public TraceCorrelationFilter traceCorrelationFilter(Tracer tracer) {
        return new TraceCorrelationFilter(tracer);
    }

    /**
     * Filter that adds trace context to MDC for logging.
     */
    public static class TraceCorrelationFilter extends OncePerRequestFilter {

        private final Tracer tracer;

        public TraceCorrelationFilter(Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       FilterChain filterChain) 
                throws ServletException, java.io.IOException {
            
            try {
                Span currentSpan = tracer.currentSpan();
                if (currentSpan != null) {
                    MDC.put("traceId", currentSpan.context().traceId());
                    MDC.put("spanId", currentSpan.context().spanId());
                }
                
                // Add request context to MDC
                MDC.put("requestUri", request.getRequestURI());
                MDC.put("requestMethod", request.getMethod());
                
                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId != null) {
                    MDC.put("correlationId", correlationId);
                }

                filterChain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        }
    }
}
