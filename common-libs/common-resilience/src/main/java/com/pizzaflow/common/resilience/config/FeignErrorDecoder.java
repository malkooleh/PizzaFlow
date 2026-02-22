package com.pizzaflow.common.resilience.config;

import com.pizzaflow.common.resilience.exception.ServiceUnavailableException;
import com.pizzaflow.common.resilience.exception.BadRequestException;
import com.pizzaflow.common.resilience.exception.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom Feign error decoder for handling service responses.
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.warn("Feign client error: method={}, status={}, reason={}",
                methodKey, response.status(), response.reason());

        return switch (response.status()) {
            case 400 -> new BadRequestException("Bad request to " + methodKey);
            case 404 -> new ResourceNotFoundException("Resource not found: " + methodKey);
            case 503, 502, 504 -> new ServiceUnavailableException(
                    "Service unavailable: " + methodKey, response.status());
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}
