package com.pizzaflow.common.grpc.config;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Client interceptor that logs all outgoing gRPC requests.
 */
public class LoggingClientInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingClientInterceptor.class);
    private static final String CORRELATION_ID_HEADER = "x-correlation-id";
    private static final Metadata.Key<String> CORRELATION_ID_KEY =
            Metadata.Key.of(CORRELATION_ID_HEADER, Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        String methodName = method.getFullMethodName();
        Instant startTime = Instant.now();
        
        // Get or generate correlation ID
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        final String finalCorrelationId = correlationId;

        log.debug("gRPC client call started: method={}, correlationId={}", methodName, correlationId);

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Add correlation ID to outgoing headers
                headers.put(CORRELATION_ID_KEY, finalCorrelationId);
                
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        Duration duration = Duration.between(startTime, Instant.now());
                        
                        if (status.isOk()) {
                            log.debug("gRPC client call completed: method={}, status={}, duration={}ms",
                                    methodName, status.getCode(), duration.toMillis());
                        } else {
                            log.warn("gRPC client call failed: method={}, status={}, duration={}ms, description={}",
                                    methodName, status.getCode(), duration.toMillis(), status.getDescription());
                        }
                        
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }
}
