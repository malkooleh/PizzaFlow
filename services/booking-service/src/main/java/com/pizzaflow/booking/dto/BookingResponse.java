package com.pizzaflow.booking.dto;

import com.pizzaflow.booking.model.Booking;
import com.pizzaflow.booking.model.enums.BookingStatus;
import com.pizzaflow.booking.model.enums.TableType;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        String bookingNumber,
        UUID customerId,
        String customerName,
        String customerPhone,
        String customerEmail,
        UUID restaurantId,
        String restaurantName,
        UUID tableId,
        String tableName,
        TableType tableType,
        LocalDateTime reservationTime,
        LocalDateTime endTime,
        int partySize,
        BookingStatus status,
        String specialRequests,
        UUID preOrderId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getBookingNumber(),
                booking.getCustomerId(),
                booking.getCustomerName(),
                booking.getCustomerPhone(),
                booking.getCustomerEmail(),
                booking.getRestaurant().getId(),
                booking.getRestaurant().getName(),
                booking.getTable() != null ? booking.getTable().getId() : null,
                booking.getTable() != null ? booking.getTable().getDisplayName() : null,
                booking.getTable() != null ? booking.getTable().getTableType() : null,
                booking.getReservationTime(),
                booking.getEndTime(),
                booking.getPartySize(),
                booking.getStatus(),
                booking.getSpecialRequests(),
                booking.getPreOrderId(),
                booking.getCreatedAt(),
                booking.getUpdatedAt());
    }
}
