package com.pizzaflow.common.grpc.config;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server interceptor that handles exceptions and converts them to appropriate gRPC statuses.
 */
public class ErrorHandlingServerInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandlingServerInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    handleException(call, e);
                }
            }

            @Override
            public void onMessage(ReqT message) {
                try {
                    super.onMessage(message);
                } catch (Exception e) {
                    handleException(call, e);
                }
            }
        };
    }

    private <ReqT, RespT> void handleException(ServerCall<ReqT, RespT> call, Exception e) {
        String methodName = call.getMethodDescriptor().getFullMethodName();
        Status status = mapExceptionToStatus(e);
        
        log.error("gRPC error in method {}: {}", methodName, e.getMessage(), e);
        
        call.close(status, new Metadata());
    }

    private Status mapExceptionToStatus(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage());
        }
        
        if (e instanceof IllegalStateException) {
            return Status.FAILED_PRECONDITION.withDescription(e.getMessage());
        }
        
        if (e instanceof SecurityException) {
            return Status.PERMISSION_DENIED.withDescription(e.getMessage());
        }
        
        if (e instanceof UnsupportedOperationException) {
            return Status.UNIMPLEMENTED.withDescription(e.getMessage());
        }
        
        if (e.getClass().getSimpleName().contains("NotFound") ||
            e.getClass().getSimpleName().contains("NoSuchElement")) {
            return Status.NOT_FOUND.withDescription(e.getMessage());
        }
        
        if (e.getClass().getSimpleName().contains("AlreadyExists") ||
            e.getClass().getSimpleName().contains("Duplicate")) {
            return Status.ALREADY_EXISTS.withDescription(e.getMessage());
        }
        
        if (e.getClass().getSimpleName().contains("Timeout")) {
            return Status.DEADLINE_EXCEEDED.withDescription(e.getMessage());
        }
        
        if (e.getClass().getSimpleName().contains("Unavailable") ||
            e.getClass().getSimpleName().contains("ServiceUnavailable")) {
            return Status.UNAVAILABLE.withDescription(e.getMessage());
        }
        
        // Default to internal error for unknown exceptions
        return Status.INTERNAL.withDescription("Internal server error: " + e.getMessage());
    }
}
