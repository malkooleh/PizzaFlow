package com.pizzaflow.inventory.model.enums;

public enum ReservationStatus {
    PENDING, // Reservation requested, not yet confirmed
    CONFIRMED, // Ingredients successfully reserved
    RELEASED, // Reservation cancelled/released
    EXPIRED // Reservation expired (time-based)
}
