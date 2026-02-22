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

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Client interceptor that handles retries for failed gRPC calls.
 * Implements exponential backoff for transient errors.
 */
public class RetryClientInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RetryClientInterceptor.class);
    
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 100;
    private static final long MAX_BACKOFF_MS = 5000;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    
    // Status codes that should trigger a retry
    private static final Set<Status.Code> RETRYABLE_STATUS_CODES = Set.of(
            Status.Code.UNAVAILABLE,
            Status.Code.RESOURCE_EXHAUSTED,
            Status.Code.ABORTED,
            Status.Code.DEADLINE_EXCEEDED
    );

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new RetryingClientCall<>(method, callOptions, next, MAX_RETRIES);
    }

    private class RetryingClientCall<ReqT, RespT> extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {
        
        private final MethodDescriptor<ReqT, RespT> method;
        private final CallOptions callOptions;
        private final Channel channel;
        private final int maxRetries;
        private int attemptCount = 0;
        private Listener<RespT> responseListener;
        private Metadata requestHeaders;
        private ReqT requestMessage;

        RetryingClientCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, 
                          Channel channel, int maxRetries) {
            super(channel.newCall(method, callOptions));
            this.method = method;
            this.callOptions = callOptions;
            this.channel = channel;
            this.maxRetries = maxRetries;
        }

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            this.responseListener = responseListener;
            this.requestHeaders = headers;
            
            super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                @Override
                public void onClose(Status status, Metadata trailers) {
                    if (shouldRetry(status)) {
                        attemptCount++;
                        long backoffMs = calculateBackoff(attemptCount);
                        
                        log.debug("gRPC call failed with status {}, retrying in {}ms (attempt {}/{})",
                                status.getCode(), backoffMs, attemptCount, maxRetries);
                        
                        try {
                            TimeUnit.MILLISECONDS.sleep(backoffMs);
                            retryCall();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            super.onClose(Status.CANCELLED.withCause(e), trailers);
                        }
                    } else {
                        super.onClose(status, trailers);
                    }
                }
            }, headers);
        }

        @Override
        public void sendMessage(ReqT message) {
            this.requestMessage = message;
            super.sendMessage(message);
        }

        private boolean shouldRetry(Status status) {
            return attemptCount < maxRetries && 
                   RETRYABLE_STATUS_CODES.contains(status.getCode());
        }

        private long calculateBackoff(int attempt) {
            long backoff = (long) (INITIAL_BACKOFF_MS * Math.pow(BACKOFF_MULTIPLIER, attempt - 1));
            // Add jitter (up to 10% variation)
            long jitter = (long) (backoff * 0.1 * Math.random());
            return Math.min(backoff + jitter, MAX_BACKOFF_MS);
        }

        private void retryCall() {
            ClientCall<ReqT, RespT> newCall = channel.newCall(method, callOptions);
            newCall.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                @Override
                public void onClose(Status status, Metadata trailers) {
                    if (shouldRetry(status)) {
                        attemptCount++;
                        long backoffMs = calculateBackoff(attemptCount);
                        
                        log.debug("gRPC retry failed with status {}, retrying in {}ms (attempt {}/{})",
                                status.getCode(), backoffMs, attemptCount, maxRetries);
                        
                        try {
                            TimeUnit.MILLISECONDS.sleep(backoffMs);
                            retryCall();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            super.onClose(Status.CANCELLED.withCause(e), trailers);
                        }
                    } else {
                        if (!status.isOk() && attemptCount > 0) {
                            log.warn("gRPC call failed after {} retries with status {}",
                                    attemptCount, status.getCode());
                        }
                        super.onClose(status, trailers);
                    }
                }
            }, requestHeaders);
            
            newCall.request(1);
            if (requestMessage != null) {
                newCall.sendMessage(requestMessage);
            }
            newCall.halfClose();
        }
    }
}
