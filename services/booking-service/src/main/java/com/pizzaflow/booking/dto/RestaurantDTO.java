package com.pizzaflow.booking.dto;

import com.pizzaflow.booking.model.Restaurant;

import java.time.LocalTime;
import java.util.UUID;

public record RestaurantDTO(
    UUID id,
    String name,
    String address,
    LocalTime openingTime,
    LocalTime closingTime,
    int maxPartySize,
    int bookingSlotDurationMinutes,
    boolean isActive
) {
    public static RestaurantDTO from(Restaurant restaurant) {
        return new RestaurantDTO(
            restaurant.getId(),
            restaurant.getName(),
            restaurant.getAddress(),
            restaurant.getOpeningTime(),
            restaurant.getClosingTime(),
            restaurant.getMaxPartySize(),
            restaurant.getBookingSlotDurationMinutes(),
            restaurant.isActive()
        );
    }
}
