package com.pizzaflow.booking.repository;

import com.pizzaflow.booking.model.Booking;
import com.pizzaflow.booking.model.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

        Optional<Booking> findByBookingNumber(String bookingNumber);

        List<Booking> findByCustomerId(UUID customerId);

        Page<Booking> findByCustomerId(UUID customerId, Pageable pageable);

        List<Booking> findByCustomerIdAndStatusIn(UUID customerId, List<BookingStatus> statuses);

        @Query("""
                        SELECT b FROM Booking b
                        JOIN FETCH b.restaurant
                        LEFT JOIN FETCH b.table
                        WHERE b.id = :id
                        """)
        Optional<Booking> findByIdWithDetails(@Param("id") UUID id);

        @Query("""
                        SELECT b FROM Booking b
                        WHERE b.restaurant.id = :restaurantId
                        AND b.status IN :statuses
                        AND b.reservationTime >= :startTime
                        AND b.reservationTime < :endTime
                        ORDER BY b.reservationTime
                        """)
        List<Booking> findByRestaurantAndTimeRange(
                        @Param("restaurantId") UUID restaurantId,
                        @Param("statuses") List<BookingStatus> statuses,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * Find conflicting bookings for a specific table (pessimistic lock for
         * double-booking prevention).
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT b FROM Booking b
                        WHERE b.table.id = :tableId
                        AND b.status IN ('PENDING', 'CONFIRMED', 'SEATED')
                        AND b.reservationTime < :endTime
                        AND b.endTime > :startTime
                        """)
        List<Booking> findConflictingBookingsForTable(
                        @Param("tableId") UUID tableId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * Count bookings at a time slot to check capacity (restaurant-wide).
         */
        @Query("""
                        SELECT COALESCE(SUM(b.partySize), 0) FROM Booking b
                        WHERE b.restaurant.id = :restaurantId
                        AND b.status IN ('PENDING', 'CONFIRMED', 'SEATED')
                        AND b.reservationTime < :endTime
                        AND b.endTime > :startTime
                        """)
        Integer countGuestsInTimeSlot(
                        @Param("restaurantId") UUID restaurantId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * Find bookings that need reminder (1 hour before).
         */
        @Query("""
                        SELECT b FROM Booking b
                        WHERE b.status = 'CONFIRMED'
                        AND b.reminderSent = false
                        AND b.reservationTime BETWEEN :fromTime AND :toTime
                        """)
        List<Booking> findBookingsNeedingReminder(
                        @Param("fromTime") LocalDateTime fromTime,
                        @Param("toTime") LocalDateTime toTime);

        /**
         * Find upcoming bookings for today's dashboard.
         */
        @Query("""
                        SELECT b FROM Booking b
                        JOIN FETCH b.restaurant
                        LEFT JOIN FETCH b.table
                        WHERE b.restaurant.id = :restaurantId
                        AND b.reservationTime >= :startOfDay
                        AND b.reservationTime < :endOfDay
                        AND b.status IN ('PENDING', 'CONFIRMED')
                        ORDER BY b.reservationTime
                        """)
        List<Booking> findTodaysBookings(
                        @Param("restaurantId") UUID restaurantId,
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay);

        @Query("""
                        SELECT b FROM Booking b
                        JOIN FETCH b.restaurant
                        LEFT JOIN FETCH b.table
                        WHERE b.restaurant.id = :restaurantId
                        AND b.reservationTime >= :startOfDay
                        AND b.reservationTime < :endOfDay
                        AND b.status IN ('PENDING', 'CONFIRMED')
                        ORDER BY b.reservationTime
                        """)
        Page<Booking> findTodaysBookings(
                        @Param("restaurantId") UUID restaurantId,
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay,
                        Pageable pageable);
}
