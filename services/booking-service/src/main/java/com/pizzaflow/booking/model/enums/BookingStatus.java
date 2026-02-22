package com.pizzaflow.booking.model.enums;

public enum BookingStatus {
    PENDING,     // Booking requested, awaiting confirmation
    CONFIRMED,   // Booking confirmed, table assigned
    CANCELLED,   // Booking cancelled by customer or restaurant
    NO_SHOW,     // Customer didn't arrive
    SEATED,      // Customer has arrived and been seated
    COMPLETED    // Dining completed
}
