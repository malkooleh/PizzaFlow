package com.pizzaflow.inventory.repository;

import com.pizzaflow.inventory.model.Reservation;
import com.pizzaflow.inventory.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByOrderId(UUID orderId);

    List<Reservation> findByOrderIdAndStatus(UUID orderId, ReservationStatus status);

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.status = :status
            AND r.reservedAt < :beforeTime
            """)
    List<Reservation> findExpiredReservations(
            @Param("status") ReservationStatus status,
            @Param("beforeTime") LocalDateTime beforeTime);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.ingredient
            WHERE r.orderId = :orderId
            """)
    List<Reservation> findByOrderIdWithIngredient(@Param("orderId") UUID orderId);

    @Query("""
            SELECT COUNT(r) FROM Reservation r
            WHERE r.orderId = :orderId
            AND r.status = :status
            """)
    long countByOrderIdAndStatus(@Param("orderId") UUID orderId, @Param("status") ReservationStatus status);
}
