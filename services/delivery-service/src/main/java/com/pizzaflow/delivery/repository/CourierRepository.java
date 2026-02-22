package com.pizzaflow.delivery.repository;

import com.pizzaflow.delivery.model.Courier;
import com.pizzaflow.delivery.model.enums.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {

    Optional<Courier> findByUserId(UUID userId);

    List<Courier> findByStatusAndIsActiveTrue(CourierStatus status);

    @Query("""
        SELECT c FROM Courier c 
        WHERE c.status = 'AVAILABLE' 
        AND c.isActive = true 
        AND c.currentLatitude IS NOT NULL 
        AND c.currentLongitude IS NOT NULL
        """)
    List<Courier> findAvailableCouriers();

    @Query(value = """
        SELECT * FROM couriers c
        WHERE c.status = 'AVAILABLE' 
        AND c.is_active = true 
        AND c.current_latitude IS NOT NULL
        ORDER BY (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(c.current_latitude)) *
                cos(radians(c.current_longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(c.current_latitude))
            )
        ) ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Courier> findNearestAvailableCouriers(
        @Param("lat") BigDecimal latitude,
        @Param("lng") BigDecimal longitude,
        @Param("limit") int limit
    );

    @Query("""
        SELECT COUNT(d) FROM Delivery d 
        WHERE d.courier.id = :courierId 
        AND d.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'ARRIVED')
        """)
    int countActiveDeliveries(@Param("courierId") UUID courierId);
}
