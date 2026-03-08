package com.pizzaflow.delivery.repository;

import com.pizzaflow.delivery.model.Delivery;
import com.pizzaflow.delivery.model.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByOrderId(UUID orderId);

    List<Delivery> findByCourierId(UUID courierId);

    List<Delivery> findByCustomerId(UUID customerId);

    List<Delivery> findByStatus(DeliveryStatus status);

    @Query("""
        SELECT d FROM Delivery d 
        JOIN FETCH d.courier 
        WHERE d.id = :id
        """)
    Optional<Delivery> findByIdWithCourier(@Param("id") UUID id);

    @Query("""
        SELECT d FROM Delivery d 
        WHERE d.courier.id = :courierId 
        AND d.status IN :statuses
        ORDER BY d.priority DESC, d.createdAt ASC
        """)
    List<Delivery> findCourierActiveDeliveries(
        @Param("courierId") UUID courierId,
        @Param("statuses") List<DeliveryStatus> statuses
    );

    @Query("""
        SELECT d FROM Delivery d 
        WHERE d.status = 'PENDING' 
        ORDER BY d.priority DESC, d.createdAt ASC
        """)
    List<Delivery> findPendingDeliveries();

    @Query("""
        SELECT d FROM Delivery d 
        WHERE d.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'ARRIVED') 
        ORDER BY d.priority DESC, d.estimatedDeliveryTime ASC
        """)
    List<Delivery> findAllActiveDeliveries();

    @Query(value = """
        SELECT d FROM Delivery d 
        WHERE d.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'ARRIVED')
        """,
        countQuery = """
        SELECT COUNT(d) FROM Delivery d 
        WHERE d.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'ARRIVED')
        """)
    Page<Delivery> findAllActiveDeliveries(Pageable pageable);
}
