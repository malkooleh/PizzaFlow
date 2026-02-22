package com.pizzaflow.booking.event;

import com.pizzaflow.booking.model.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingEvent(
    String eventType,
    UUID bookingId,
    String bookingNumber,
    UUID customerId,
    String customerName,
    String customerEmail,
    String customerPhone,
    UUID restaurantId,
    String restaurantName,
    UUID tableId,
    String tableName,
    LocalDateTime reservationTime,
    LocalDateTime endTime,
    int partySize,
    BookingStatus status,
    UUID preOrderId,
    LocalDateTime eventTimestamp
) {
    public static final String BOOKING_CREATED = "booking.created";
    public static final String BOOKING_CONFIRMED = "booking.confirmed";
    public static final String BOOKING_CANCELLED = "booking.cancelled";
    public static final String BOOKING_REMINDER = "booking.reminder";
    public static final String BOOKING_SEATED = "booking.seated";
    public static final String BOOKING_COMPLETED = "booking.completed";
    public static final String BOOKING_NO_SHOW = "booking.no_show";
}
