package com.pizzaflow.common.observability.kafka;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * Utility for propagating trace context through Kafka messages.
 * 
 * <p>Provides methods to:
 * <ul>
 *   <li>Extract trace IDs from incoming Kafka messages</li>
 *   <li>Inject trace IDs into outgoing Kafka messages</li>
 *   <li>Set up MDC context for Kafka listener processing</li>
 * </ul>
 */
public class KafkaTracingUtils {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String SPAN_ID_HEADER = "X-Span-Id";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final Tracer tracer;

    public KafkaTracingUtils(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Sets up MDC context for processing a Kafka message.
     * Call this at the start of your @KafkaListener method.
     * 
     * @param record The incoming Kafka consumer record
     * @return A context that should be closed when processing completes
     */
    public KafkaTracingContext setupTracingContext(ConsumerRecord<?, ?> record) {
        String traceId = extractHeader(record, TRACE_ID_HEADER)
                .orElseGet(() -> generateTraceId());
        String spanId = extractHeader(record, SPAN_ID_HEADER)
                .orElseGet(() -> generateSpanId());
        String correlationId = extractHeader(record, CORRELATION_ID_HEADER)
                .orElse(traceId);

        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);
        MDC.put("correlationId", correlationId);
        MDC.put("kafkaTopic", record.topic());
        MDC.put("kafkaPartition", String.valueOf(record.partition()));
        MDC.put("kafkaOffset", String.valueOf(record.offset()));

        return new KafkaTracingContext();
    }

    /**
     * Injects trace context headers into an outgoing Kafka message.
     * 
     * @param record The outgoing producer record
     * @param <K> Key type
     * @param <V> Value type
     * @return The record with trace headers added
     */
    public <K, V> ProducerRecord<K, V> injectTracingHeaders(ProducerRecord<K, V> record) {
        Span currentSpan = tracer.currentSpan();
        
        if (currentSpan != null) {
            record.headers().add(TRACE_ID_HEADER, 
                    currentSpan.context().traceId().getBytes(StandardCharsets.UTF_8));
            record.headers().add(SPAN_ID_HEADER, 
                    currentSpan.context().spanId().getBytes(StandardCharsets.UTF_8));
        } else {
            // Generate new trace context if none exists
            String traceId = generateTraceId();
            record.headers().add(TRACE_ID_HEADER, traceId.getBytes(StandardCharsets.UTF_8));
            record.headers().add(SPAN_ID_HEADER, generateSpanId().getBytes(StandardCharsets.UTF_8));
        }

        // Propagate correlation ID from MDC if present
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            record.headers().add(CORRELATION_ID_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        }

        return record;
    }

    /**
     * Gets the current trace ID from MDC or generates a new one.
     */
    public String getCurrentTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : generateTraceId();
    }

    /**
     * Gets the current correlation ID from MDC.
     */
    public String getCurrentCorrelationId() {
        return MDC.get("correlationId");
    }

    private Optional<String> extractHeader(ConsumerRecord<?, ?> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header != null && header.value() != null) {
            return Optional.of(new String(header.value(), StandardCharsets.UTF_8));
        }
        return Optional.empty();
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Context object for Kafka tracing that clears MDC on close.
     * Use with try-with-resources.
     */
    public static class KafkaTracingContext implements AutoCloseable {
        @Override
        public void close() {
            MDC.clear();
        }
    }
}
