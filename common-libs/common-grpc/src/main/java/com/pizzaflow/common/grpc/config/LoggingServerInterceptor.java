package com.pizzaflow.common.grpc.config;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Server interceptor that logs all gRPC requests and responses.
 */
public class LoggingServerInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingServerInterceptor.class);
    private static final String CORRELATION_ID_HEADER = "x-correlation-id";
    private static final Metadata.Key<String> CORRELATION_ID_KEY =
            Metadata.Key.of(CORRELATION_ID_HEADER, Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String correlationId = headers.get(CORRELATION_ID_KEY);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        String methodName = call.getMethodDescriptor().getFullMethodName();
        Instant startTime = Instant.now();

        // Set MDC context for logging
        MDC.put("correlationId", correlationId);
        MDC.put("grpcMethod", methodName);

        log.info("gRPC request started: method={}", methodName);

        final String finalCorrelationId = correlationId;

        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                Duration duration = Duration.between(startTime, Instant.now());
                
                MDC.put("correlationId", finalCorrelationId);
                MDC.put("grpcMethod", methodName);
                MDC.put("grpcStatus", status.getCode().name());
                MDC.put("durationMs", String.valueOf(duration.toMillis()));

                if (status.isOk()) {
                    log.info("gRPC request completed: method={}, status={}, duration={}ms",
                            methodName, status.getCode(), duration.toMillis());
                } else {
                    log.warn("gRPC request failed: method={}, status={}, duration={}ms, description={}",
                            methodName, status.getCode(), duration.toMillis(), status.getDescription());
                }

                // Add correlation ID to response trailers
                trailers.put(CORRELATION_ID_KEY, finalCorrelationId);
                
                MDC.clear();
                super.close(status, trailers);
            }
        };

        return next.startCall(wrappedCall, headers);
    }
}
