package com.pizzaflow.common.observability.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility class for creating and managing trace spans.
 */
@Component
public class TracingUtils {

    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;

    public TracingUtils(Tracer tracer, ObservationRegistry observationRegistry) {
        this.tracer = tracer;
        this.observationRegistry = observationRegistry;
    }

    /**
     * Creates a new span for tracing an operation.
     */
    public Span createSpan(String name) {
        return tracer.nextSpan().name(name).start();
    }

    /**
     * Creates a new span with custom tags.
     */
    public Span createSpan(String name, Map<String, String> tags) {
        Span span = tracer.nextSpan().name(name);
        tags.forEach(span::tag);
        return span.start();
    }

    /**
     * Executes an operation within a new span.
     */
    public <T> T traceOperation(String name, Supplier<T> operation) {
        Span span = createSpan(name);
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            return operation.get();
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Executes an operation within a new span with tags.
     */
    public <T> T traceOperation(String name, Map<String, String> tags, Supplier<T> operation) {
        Span span = createSpan(name, tags);
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            return operation.get();
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Executes a runnable within a new span.
     */
    public void traceRunnable(String name, Runnable runnable) {
        Span span = createSpan(name);
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            runnable.run();
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Creates an observation for detailed metrics and tracing.
     */
    public Observation createObservation(String name) {
        return Observation.createNotStarted(name, observationRegistry);
    }

    /**
     * Creates an observation with a context key-value.
     */
    public Observation createObservation(String name, String contextKey, String contextValue) {
        return Observation.createNotStarted(name, observationRegistry)
                .lowCardinalityKeyValue(contextKey, contextValue);
    }

    /**
     * Gets the current trace ID.
     */
    public String getCurrentTraceId() {
        Span currentSpan = tracer.currentSpan();
        return currentSpan != null ? currentSpan.context().traceId() : null;
    }

    /**
     * Gets the current span ID.
     */
    public String getCurrentSpanId() {
        Span currentSpan = tracer.currentSpan();
        return currentSpan != null ? currentSpan.context().spanId() : null;
    }

    /**
     * Adds a tag to the current span.
     */
    public void tagCurrentSpan(String key, String value) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag(key, value);
        }
    }

    /**
     * Adds an event to the current span.
     */
    public void addEventToCurrentSpan(String name) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.event(name);
        }
    }

    /**
     * Records an error on the current span.
     */
    public void recordErrorOnCurrentSpan(Throwable error) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.error(error);
        }
    }
}
