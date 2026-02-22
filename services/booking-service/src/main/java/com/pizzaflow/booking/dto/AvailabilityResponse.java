package com.pizzaflow.booking.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
    UUID restaurantId,
    String restaurantName,
    LocalDate date,
    int requestedPartySize,
    List<TimeSlotDTO> availableSlots,
    int totalCapacity,
    boolean fullyBooked
) {
}
