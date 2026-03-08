package com.pizzaflow.delivery.service;

import com.pizzaflow.delivery.dto.CourierResponse;
import com.pizzaflow.delivery.dto.LocationUpdateRequest;
import com.pizzaflow.delivery.exception.CourierNotFoundException;
import com.pizzaflow.delivery.model.Courier;
import com.pizzaflow.delivery.model.enums.CourierStatus;
import com.pizzaflow.delivery.repository.CourierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CourierService {

    private static final Logger log = LoggerFactory.getLogger(CourierService.class);

    private final CourierRepository courierRepository;

    @Value("${delivery.max-deliveries-per-courier:3}")
    private int maxDeliveriesPerCourier;

    public CourierService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Transactional(readOnly = true)
    public CourierResponse getCourier(UUID courierId) {
        Courier courier = findCourierById(courierId);
        return CourierResponse.from(courier);
    }

    @Transactional(readOnly = true)
    public CourierResponse getCourierByUserId(UUID userId) {
        Courier courier = courierRepository.findByUserId(userId)
            .orElseThrow(() -> new CourierNotFoundException("Courier not found for user: " + userId));
        return CourierResponse.from(courier);
    }

    @Transactional(readOnly = true)
    public Page<CourierResponse> getAvailableCouriers(Pageable pageable) {
        return courierRepository.findAvailableCouriers(pageable)
            .map(CourierResponse::from);
    }

    @Transactional
    public CourierResponse updateStatus(UUID courierId, CourierStatus status) {
        Courier courier = findCourierById(courierId);
        courier.setStatus(status);
        courier = courierRepository.save(courier);
        log.info("Courier {} status updated to {}", courierId, status);
        return CourierResponse.from(courier);
    }

    @Transactional
    public CourierResponse updateLocation(UUID courierId, LocationUpdateRequest request) {
        Courier courier = findCourierById(courierId);
        courier.updateLocation(request.latitude(), request.longitude());
        courier = courierRepository.save(courier);
        log.debug("Courier {} location updated: ({}, {})", courierId, request.latitude(), request.longitude());
        return CourierResponse.from(courier);
    }

    @Transactional
    public CourierResponse goOnline(UUID courierId, BigDecimal latitude, BigDecimal longitude) {
        Courier courier = findCourierById(courierId);
        courier.setStatus(CourierStatus.AVAILABLE);
        courier.updateLocation(latitude, longitude);
        courier = courierRepository.save(courier);
        log.info("Courier {} is now online at ({}, {})", courierId, latitude, longitude);
        return CourierResponse.from(courier);
    }

    @Transactional
    public CourierResponse goOffline(UUID courierId) {
        Courier courier = findCourierById(courierId);

        // Check if courier has active deliveries
        int activeDeliveries = courierRepository.countActiveDeliveries(courierId);
        if (activeDeliveries > 0) {
            throw new IllegalStateException(
                "Cannot go offline with active deliveries. Complete or reassign " + activeDeliveries + " deliveries first.");
        }

        courier.setStatus(CourierStatus.OFFLINE);
        courier = courierRepository.save(courier);
        log.info("Courier {} is now offline", courierId);
        return CourierResponse.from(courier);
    }

    /**
     * Find the best available courier for a pickup location.
     */
    @Transactional(readOnly = true)
    public Courier findBestCourierForDelivery(BigDecimal pickupLatitude, BigDecimal pickupLongitude) {
        List<Courier> nearestCouriers = courierRepository.findNearestAvailableCouriers(
            pickupLatitude, pickupLongitude, 10
        );

        for (Courier courier : nearestCouriers) {
            int activeDeliveries = courierRepository.countActiveDeliveries(courier.getId());
            if (activeDeliveries < maxDeliveriesPerCourier) {
                return courier;
            }
        }

        return null;  // No available courier
    }

    @Transactional
    public void incrementDeliveryCount(UUID courierId) {
        Courier courier = findCourierById(courierId);
        courier.setTotalDeliveries(courier.getTotalDeliveries() + 1);
        courierRepository.save(courier);
    }

    private Courier findCourierById(UUID courierId) {
        return courierRepository.findById(courierId)
            .orElseThrow(() -> new CourierNotFoundException("Courier not found: " + courierId));
    }
}
