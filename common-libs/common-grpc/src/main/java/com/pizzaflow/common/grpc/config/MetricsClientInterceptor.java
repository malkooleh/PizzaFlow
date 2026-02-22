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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;

/**
 * Client interceptor that records metrics for outgoing gRPC calls.
 */
public class MetricsClientInterceptor implements ClientInterceptor {

    private MeterRegistry meterRegistry;

    @Autowired
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        String serviceName = extractServiceName(method.getFullMethodName());
        String methodName = extractMethodName(method.getFullMethodName());
        Instant startTime = Instant.now();

        // Increment request counter
        if (meterRegistry != null) {
            Counter.builder("grpc.client.requests")
                    .tag("service", serviceName)
                    .tag("method", methodName)
                    .description("Total number of outgoing gRPC requests")
                    .register(meterRegistry)
                    .increment();
        }

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        if (meterRegistry != null) {
                            Duration duration = Duration.between(startTime, Instant.now());

                            // Record response time
                            Timer.builder("grpc.client.duration")
                                    .tag("service", serviceName)
                                    .tag("method", methodName)
                                    .tag("status", status.getCode().name())
                                    .description("gRPC client call duration")
                                    .register(meterRegistry)
                                    .record(duration);

                            // Increment response counter by status
                            Counter.builder("grpc.client.responses")
                                    .tag("service", serviceName)
                                    .tag("method", methodName)
                                    .tag("status", status.getCode().name())
                                    .description("Total number of gRPC client responses by status")
                                    .register(meterRegistry)
                                    .increment();

                            // Track errors separately
                            if (!status.isOk()) {
                                Counter.builder("grpc.client.errors")
                                        .tag("service", serviceName)
                                        .tag("method", methodName)
                                        .tag("status", status.getCode().name())
                                        .description("Total number of gRPC client errors")
                                        .register(meterRegistry)
                                        .increment();
                            }
                        }

                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
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
