package com.pizzaflow.booking.service;

import com.pizzaflow.booking.dto.AvailabilityResponse;
import com.pizzaflow.booking.dto.TimeSlotDTO;
import com.pizzaflow.booking.exception.RestaurantNotFoundException;
import com.pizzaflow.booking.model.Booking;
import com.pizzaflow.booking.model.Restaurant;
import com.pizzaflow.booking.model.TableConfiguration;
import com.pizzaflow.booking.model.enums.BookingStatus;
import com.pizzaflow.booking.repository.BlockedSlotRepository;
import com.pizzaflow.booking.repository.BookingRepository;
import com.pizzaflow.booking.repository.RestaurantRepository;
import com.pizzaflow.booking.repository.TableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityService.class);

    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(
            BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.SEATED);

    private final RestaurantRepository restaurantRepository;
    private final TableRepository tableRepository;
    private final BookingRepository bookingRepository;
    private final BlockedSlotRepository blockedSlotRepository;

    public AvailabilityService(
            RestaurantRepository restaurantRepository,
            TableRepository tableRepository,
            BookingRepository bookingRepository,
            BlockedSlotRepository blockedSlotRepository) {
        this.restaurantRepository = restaurantRepository;
        this.tableRepository = tableRepository;
        this.bookingRepository = bookingRepository;
        this.blockedSlotRepository = blockedSlotRepository;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(UUID restaurantId, LocalDate date, int partySize) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(
                        "Restaurant not found: " + restaurantId));

        if (!restaurant.isActive()) {
            throw new RestaurantNotFoundException("Restaurant is not active: " + restaurantId);
        }

        List<TableConfiguration> suitableTables = tableRepository
                .findSuitableTablesForPartySize(restaurantId, partySize);

        LocalDateTime startOfDay = date.atTime(restaurant.getOpeningTime());
        LocalDateTime endOfDay = date.atTime(restaurant.getClosingTime());

        // Handle overnight restaurants (closing after midnight)
        if (restaurant.getClosingTime().isBefore(restaurant.getOpeningTime())) {
            endOfDay = date.plusDays(1).atTime(restaurant.getClosingTime());
        }

        List<Booking> existingBookings = bookingRepository.findByRestaurantAndTimeRange(
                restaurantId, ACTIVE_STATUSES, startOfDay, endOfDay);

        List<TimeSlotDTO> availableSlots = calculateAvailableSlots(
                restaurant, suitableTables, existingBookings, date, partySize);

        Integer totalCapacity = tableRepository.getTotalCapacity(restaurantId);

        boolean fullyBooked = availableSlots.isEmpty();

        log.debug("Found {} available slots for restaurant {} on {} for party of {}",
                availableSlots.size(), restaurantId, date, partySize);

        return new AvailabilityResponse(
                restaurantId,
                restaurant.getName(),
                date,
                partySize,
                availableSlots,
                totalCapacity != null ? totalCapacity : 0,
                fullyBooked);
    }

    private List<TimeSlotDTO> calculateAvailableSlots(
            Restaurant restaurant,
            List<TableConfiguration> suitableTables,
            List<Booking> existingBookings,
            LocalDate date,
            int partySize) {
        List<TimeSlotDTO> slots = new ArrayList<>();
        int slotDuration = restaurant.getBookingSlotDurationMinutes();

        LocalTime opening = restaurant.getOpeningTime();
        LocalTime closing = restaurant.getClosingTime();

        // Generate time slots throughout the day
        LocalTime currentTime = opening;
        LocalDateTime now = LocalDateTime.now();

        while (currentTime.isBefore(closing) ||
                (closing.isBefore(opening) && currentTime.isAfter(opening))) {

            LocalDateTime slotStart = date.atTime(currentTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(slotDuration);

            // Skip past slots
            if (slotStart.isAfter(now.minusMinutes(30))) {
                List<TimeSlotDTO.AvailableTableDTO> availableTables = findAvailableTablesForSlot(suitableTables,
                        existingBookings, slotStart, slotEnd);

                if (!availableTables.isEmpty()) {
                    int availableCapacity = availableTables.stream()
                            .mapToInt(TimeSlotDTO.AvailableTableDTO::capacity)
                            .sum();

                    slots.add(new TimeSlotDTO(slotStart, slotEnd, availableCapacity, availableTables));
                }
            }

            currentTime = currentTime.plusMinutes(30); // 30-minute increments

            // Prevent infinite loop for overnight restaurants
            if (closing.isBefore(opening) && currentTime.isBefore(opening) && currentTime.isAfter(closing)) {
                break;
            }
        }

        return slots;
    }

    private List<TimeSlotDTO.AvailableTableDTO> findAvailableTablesForSlot(
            List<TableConfiguration> tables,
            List<Booking> existingBookings,
            LocalDateTime slotStart,
            LocalDateTime slotEnd) {
        return tables.stream()
                .filter(table -> isTableAvailable(table, existingBookings, slotStart, slotEnd))
                .map(table -> new TimeSlotDTO.AvailableTableDTO(
                        table.getId(),
                        table.getDisplayName(),
                        table.getCapacity(),
                        table.getTableType()))
                .toList();
    }

    private boolean isTableAvailable(
            TableConfiguration table,
            List<Booking> existingBookings,
            LocalDateTime slotStart,
            LocalDateTime slotEnd) {
        return existingBookings.stream()
                .filter(booking -> booking.getTable() != null &&
                        booking.getTable().getId().equals(table.getId()))
                .noneMatch(booking -> booking.overlaps(slotStart, slotEnd));
    }

    @Transactional(readOnly = true)
    public TableConfiguration findBestAvailableTable(
            UUID restaurantId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int partySize,
            UUID preferredTableId) {
        List<TableConfiguration> suitableTables = tableRepository
                .findSuitableTablesForPartySize(restaurantId, partySize);

        if (suitableTables.isEmpty()) {
            return null;
        }

        // Check preferred table first
        if (preferredTableId != null) {
            TableConfiguration preferred = suitableTables.stream()
                    .filter(t -> t.getId().equals(preferredTableId))
                    .findFirst()
                    .orElse(null);

            if (preferred != null && isTableAvailableNow(preferred, startTime, endTime)) {
                return preferred;
            }
        }

        // Find the smallest suitable available table
        return suitableTables.stream()
                .filter(t -> isTableAvailableNow(t, startTime, endTime))
                .findFirst()
                .orElse(null);
    }

    private boolean isTableAvailableNow(
            TableConfiguration table,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        List<Booking> conflicts = bookingRepository.findConflictingBookingsForTable(
                table.getId(), startTime, endTime);
        return conflicts.isEmpty();
    }
}
