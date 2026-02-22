package com.pizzaflow.booking.service;

import com.pizzaflow.booking.kafka.BookingEventProducer;
import com.pizzaflow.booking.model.Booking;
import com.pizzaflow.booking.model.enums.BookingStatus;
import com.pizzaflow.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    private final BookingRepository bookingRepository;
    private final BookingEventProducer eventProducer;

    @Value("${booking.reminder-before-minutes:60}")
    private int reminderBeforeMinutes;

    public ReminderService(
        BookingRepository bookingRepository,
        BookingEventProducer eventProducer
    ) {
        this.bookingRepository = bookingRepository;
        this.eventProducer = eventProducer;
    }

    /**
     * Send reminders for upcoming bookings.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000)  // 5 minutes
    @Transactional
    public void sendBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderWindow = now.plusMinutes(reminderBeforeMinutes);

        List<Booking> bookingsNeedingReminder = bookingRepository
            .findBookingsNeedingReminder(now, reminderWindow);

        for (Booking booking : bookingsNeedingReminder) {
            try {
                eventProducer.sendBookingReminder(booking);
                booking.setReminderSent(true);
                bookingRepository.save(booking);
                log.info("Sent reminder for booking {} at {}",
                    booking.getBookingNumber(), booking.getReservationTime());
            } catch (Exception e) {
                log.error("Failed to send reminder for booking {}: {}",
                    booking.getBookingNumber(), e.getMessage());
            }
        }

        if (!bookingsNeedingReminder.isEmpty()) {
            log.info("Processed {} booking reminders", bookingsNeedingReminder.size());
        }
    }

    /**
     * Mark no-shows for confirmed bookings past their reservation time.
     * Runs every 15 minutes.
     */
    @Scheduled(fixedRate = 900000)  // 15 minutes
    @Transactional
    public void markNoShows() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);

        List<Booking> overdueBookings = bookingRepository.findByRestaurantAndTimeRange(
            null,  // All restaurants
            List.of(BookingStatus.CONFIRMED),
            cutoffTime.minusHours(2),
            cutoffTime
        );

        // This is a simplified version - in production, you'd want
        // restaurant staff to manually mark no-shows with better logic
        log.debug("Found {} potentially overdue bookings", overdueBookings.size());
    }
}
