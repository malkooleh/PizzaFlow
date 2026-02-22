package com.pizzaflow.booking.dto;

import com.pizzaflow.booking.model.enums.TableType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingRequest(
    @NotNull(message = "Customer ID is required")
    UUID customerId,

    @NotNull(message = "Restaurant ID is required")
    UUID restaurantId,

    @NotNull(message = "Reservation time is required")
    @Future(message = "Reservation time must be in the future")
    LocalDateTime reservationTime,

    @Min(value = 1, message = "Party size must be at least 1")
    @Max(value = 20, message = "Party size cannot exceed 20")
    int partySize,

    String customerName,

    String customerPhone,

    String customerEmail,

    String specialRequests,

    TableType preferredTableType,

    UUID preferredTableId,

    Integer durationMinutes  // Optional, defaults to restaurant's default
) {
    public BookingRequest {
        if (durationMinutes != null && durationMinutes < 30) {
            throw new IllegalArgumentException("Duration must be at least 30 minutes");
        }
    }
}
