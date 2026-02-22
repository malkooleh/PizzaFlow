package com.pizzaflow.booking.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaflow.booking.event.BookingEvent;
import com.pizzaflow.booking.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BookingEventProducer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventProducer.class);

    private static final String BOOKING_TOPIC = "booking-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public BookingEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendBookingCreated(Booking booking) {
        sendEvent(BookingEvent.BOOKING_CREATED, booking);
    }

    public void sendBookingConfirmed(Booking booking) {
        sendEvent(BookingEvent.BOOKING_CONFIRMED, booking);
    }

    public void sendBookingCancelled(Booking booking) {
        sendEvent(BookingEvent.BOOKING_CANCELLED, booking);
    }

    public void sendBookingReminder(Booking booking) {
        sendEvent(BookingEvent.BOOKING_REMINDER, booking);
    }

    public void sendBookingSeated(Booking booking) {
        sendEvent(BookingEvent.BOOKING_SEATED, booking);
    }

    public void sendBookingCompleted(Booking booking) {
        sendEvent(BookingEvent.BOOKING_COMPLETED, booking);
    }

    public void sendBookingNoShow(Booking booking) {
        sendEvent(BookingEvent.BOOKING_NO_SHOW, booking);
    }

    private void sendEvent(String eventType, Booking booking) {
        BookingEvent event = new BookingEvent(
                eventType,
                booking.getId(),
                booking.getBookingNumber(),
                booking.getCustomerId(),
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                booking.getCustomerPhone(),
                booking.getRestaurant().getId(),
                booking.getRestaurant().getName(),
                booking.getTable() != null ? booking.getTable().getId() : null,
                booking.getTable() != null ? booking.getTable().getDisplayName() : null,
                booking.getReservationTime(),
                booking.getEndTime(),
                booking.getPartySize(),
                booking.getStatus(),
                booking.getPreOrderId(),
                LocalDateTime.now());

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(BOOKING_TOPIC, booking.getId().toString(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send {} event for booking {}: {}",
                                    eventType, booking.getBookingNumber(), ex.getMessage());
                        } else {
                            log.info("Sent {} event for booking {} to partition {}",
                                    eventType, booking.getBookingNumber(),
                                    result.getRecordMetadata().partition());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize booking event: {}", e.getMessage());
        }
    }
}
