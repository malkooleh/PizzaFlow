package com.pizzaflow.common.grpc.config;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;

/**
 * Server interceptor that records metrics for all gRPC calls.
 */
public class MetricsServerInterceptor implements ServerInterceptor {

    private MeterRegistry meterRegistry;

    @Autowired
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String serviceName = extractServiceName(call.getMethodDescriptor().getFullMethodName());
        String methodName = extractMethodName(call.getMethodDescriptor().getFullMethodName());
        Instant startTime = Instant.now();

        // Increment request counter
        if (meterRegistry != null) {
            Counter.builder("grpc.server.requests")
                    .tag("service", serviceName)
                    .tag("method", methodName)
                    .description("Total number of gRPC requests")
                    .register(meterRegistry)
                    .increment();
        }

        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                if (meterRegistry != null) {
                    Duration duration = Duration.between(startTime, Instant.now());

                    // Record response time
                    Timer.builder("grpc.server.duration")
                            .tag("service", serviceName)
                            .tag("method", methodName)
                            .tag("status", status.getCode().name())
                            .description("gRPC request duration")
                            .register(meterRegistry)
                            .record(duration);

                    // Increment response counter by status
                    Counter.builder("grpc.server.responses")
                            .tag("service", serviceName)
                            .tag("method", methodName)
                            .tag("status", status.getCode().name())
                            .description("Total number of gRPC responses by status")
                            .register(meterRegistry)
                            .increment();

                    // Track errors separately
                    if (!status.isOk()) {
                        Counter.builder("grpc.server.errors")
                                .tag("service", serviceName)
                                .tag("method", methodName)
                                .tag("status", status.getCode().name())
                                .description("Total number of gRPC errors")
                                .register(meterRegistry)
                                .increment();
                    }
                }

                super.close(status, trailers);
            }
        };

        return next.startCall(wrappedCall, headers);
    }

    private String extractServiceName(String fullMethodName) {
        int lastSlash = fullMethodName.lastIndexOf('/');
        if (lastSlash > 0) {
            return fullMethodName.substring(0, lastSlash);
        }
        return fullMethodName;
    }

    private String extractMethodName(String fullMethodName) {
        int lastSlash = fullMethodName.lastIndexOf('/');
        if (lastSlash > 0 && lastSlash < fullMethodName.length() - 1) {
            return fullMethodName.substring(lastSlash + 1);
        }
        return fullMethodName;
    }
}
