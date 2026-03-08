package com.pizzaflow.booking.service;

import com.pizzaflow.booking.dto.BookingRequest;
import com.pizzaflow.booking.dto.BookingResponse;
import com.pizzaflow.booking.exception.BookingConflictException;
import com.pizzaflow.booking.exception.BookingNotFoundException;
import com.pizzaflow.booking.exception.InvalidBookingStateException;
import com.pizzaflow.booking.exception.RestaurantNotFoundException;
import com.pizzaflow.booking.kafka.BookingEventProducer;
import com.pizzaflow.booking.model.Booking;
import com.pizzaflow.booking.model.Restaurant;
import com.pizzaflow.booking.model.TableConfiguration;
import com.pizzaflow.booking.model.enums.BookingStatus;
import com.pizzaflow.booking.repository.BookingRepository;
import com.pizzaflow.booking.repository.RestaurantRepository;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Observed(name = "booking.service", contextualName = "booking-service")
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final DateTimeFormatter BOOKING_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final Random RANDOM = new Random();

    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;
    private final AvailabilityService availabilityService;
    private final BookingEventProducer eventProducer;

    @Value("${booking.default-duration-minutes:90}")
    private int defaultDurationMinutes;

    public BookingService(
            BookingRepository bookingRepository,
            RestaurantRepository restaurantRepository,
            AvailabilityService availabilityService,
            BookingEventProducer eventProducer) {
        this.bookingRepository = bookingRepository;
        this.restaurantRepository = restaurantRepository;
        this.availabilityService = availabilityService;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for customer {} at restaurant {} on {}",
                request.customerId(), request.restaurantId(), request.reservationTime());

        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(
                        "Restaurant not found: " + request.restaurantId()));

        validateBookingRequest(request, restaurant);

        int duration = request.durationMinutes() != null
                ? request.durationMinutes()
                : defaultDurationMinutes;

        LocalDateTime endTime = request.reservationTime().plusMinutes(duration);

        // Find available table (with pessimistic locking)
        TableConfiguration table = availabilityService.findBestAvailableTable(
                request.restaurantId(),
                request.reservationTime(),
                endTime,
                request.partySize(),
                request.preferredTableId());

        if (table == null) {
            throw new BookingConflictException(
                    "No available tables for the requested time and party size");
        }

        Booking booking = new Booking();
        booking.setBookingNumber(generateBookingNumber());
        booking.setCustomerId(request.customerId());
        booking.setCustomerName(request.customerName());
        booking.setCustomerPhone(request.customerPhone());
        booking.setCustomerEmail(request.customerEmail());
        booking.setRestaurant(restaurant);
        booking.setTable(table);
        booking.setReservationTime(request.reservationTime());
        booking.setEndTime(endTime);
        booking.setPartySize(request.partySize());
        booking.setSpecialRequests(request.specialRequests());
        booking.setStatus(BookingStatus.PENDING);

        booking = bookingRepository.save(booking);
        log.info("Created booking {} for table {} at {}",
                booking.getBookingNumber(), table.getDisplayName(), request.reservationTime());

        eventProducer.sendBookingCreated(booking);

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse confirmBooking(UUID bookingId) {
        Booking booking = getBookingWithDetails(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingStateException(
                    "Can only confirm PENDING bookings, current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

        log.info("Confirmed booking {}", booking.getBookingNumber());
        eventProducer.sendBookingConfirmed(booking);

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId, String cancellationReason) {
        Booking booking = getBookingWithDetails(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED ||
                booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingStateException(
                    "Cannot cancel booking with status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(cancellationReason);
        booking = bookingRepository.save(booking);

        log.info("Cancelled booking {}: {}", booking.getBookingNumber(), cancellationReason);
        eventProducer.sendBookingCancelled(booking);

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse seatGuests(UUID bookingId) {
        Booking booking = getBookingWithDetails(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException(
                    "Can only seat CONFIRMED bookings, current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.SEATED);
        booking.setActualArrivalTime(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        log.info("Seated guests for booking {}", booking.getBookingNumber());
        eventProducer.sendBookingSeated(booking);

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse completeBooking(UUID bookingId) {
        Booking booking = getBookingWithDetails(bookingId);

        if (booking.getStatus() != BookingStatus.SEATED) {
            throw new InvalidBookingStateException(
                    "Can only complete SEATED bookings, current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking = bookingRepository.save(booking);

        log.info("Completed booking {}", booking.getBookingNumber());
        eventProducer.sendBookingCompleted(booking);

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse markNoShow(UUID bookingId) {
        Booking booking = getBookingWithDetails(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException(
                    "Can only mark no-show for CONFIRMED bookings, current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.NO_SHOW);
        booking = bookingRepository.save(booking);

        log.info("Marked no-show for booking {}", booking.getBookingNumber());
        eventProducer.sendBookingNoShow(booking);

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse linkPreOrder(UUID bookingId, UUID preOrderId) {
        Booking booking = getBookingWithDetails(bookingId);

        if (booking.getPreOrderId() != null) {
            throw new BookingConflictException(
                    "Booking already has a pre-order linked: " + booking.getPreOrderId());
        }

        booking.setPreOrderId(preOrderId);
        booking = bookingRepository.save(booking);

        log.info("Linked pre-order {} to booking {}", preOrderId, booking.getBookingNumber());

        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID bookingId) {
        return BookingResponse.from(getBookingWithDetails(bookingId));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByNumber(String bookingNumber) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found: " + bookingNumber));
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getCustomerBookings(UUID customerId, Pageable pageable) {
        return bookingRepository.findByCustomerId(customerId, pageable)
                .map(BookingResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getTodaysBookings(UUID restaurantId, Pageable pageable) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return bookingRepository.findTodaysBookings(restaurantId, startOfDay, endOfDay, pageable)
                .map(BookingResponse::from);
    }

    private Booking getBookingWithDetails(UUID bookingId) {
        return bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found: " + bookingId));
    }

    private void validateBookingRequest(BookingRequest request, Restaurant restaurant) {
        if (request.reservationTime().isBefore(LocalDateTime.now())) {
            throw new InvalidBookingStateException("Reservation time must be in the future");
        }

        if (request.partySize() > restaurant.getMaxPartySize()) {
            throw new InvalidBookingStateException(
                    "Party size exceeds maximum of " + restaurant.getMaxPartySize());
        }

        // Check if reservation time is within operating hours
        var reservationTime = request.reservationTime().toLocalTime();
        if (reservationTime.isBefore(restaurant.getOpeningTime()) ||
                reservationTime.isAfter(restaurant.getClosingTime().minusHours(1))) {
            throw new InvalidBookingStateException(
                    "Reservation time must be during operating hours (" +
                            restaurant.getOpeningTime() + " - " + restaurant.getClosingTime() + ")");
        }
    }

    private String generateBookingNumber() {
        String datePrefix = LocalDateTime.now().format(BOOKING_NUMBER_FORMAT);
        String randomSuffix = String.format("%04d", RANDOM.nextInt(10000));
        return "BK-" + datePrefix + "-" + randomSuffix;
    }
}
