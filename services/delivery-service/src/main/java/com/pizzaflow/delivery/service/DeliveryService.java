package com.pizzaflow.delivery.service;

import com.pizzaflow.delivery.dto.CreateDeliveryRequest;
import com.pizzaflow.delivery.dto.DeliveryResponse;
import com.pizzaflow.delivery.dto.LocationUpdateRequest;
import com.pizzaflow.delivery.dto.TrackingInfo;
import com.pizzaflow.delivery.exception.DeliveryNotFoundException;
import com.pizzaflow.delivery.exception.InvalidDeliveryStateException;
import com.pizzaflow.delivery.kafka.DeliveryEventProducer;
import com.pizzaflow.delivery.model.Courier;
import com.pizzaflow.delivery.model.Delivery;
import com.pizzaflow.delivery.model.DeliveryLocationHistory;
import com.pizzaflow.delivery.model.enums.CourierStatus;
import com.pizzaflow.delivery.model.enums.DeliveryPriority;
import com.pizzaflow.delivery.model.enums.DeliveryStatus;
import com.pizzaflow.delivery.repository.DeliveryRepository;
import com.pizzaflow.delivery.repository.LocationHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryRepository deliveryRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final CourierService courierService;
    private final DeliveryEventProducer eventProducer;

    @Value("${delivery.base-preparation-minutes:20}")
    private int basePreparationMinutes;

    @Value("${delivery.minutes-per-km:3}")
    private int minutesPerKm;

    @Value("${delivery.auto-assign-enabled:true}")
    private boolean autoAssignEnabled;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            LocationHistoryRepository locationHistoryRepository,
            CourierService courierService,
            DeliveryEventProducer eventProducer) {
        this.deliveryRepository = deliveryRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.courierService = courierService;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public DeliveryResponse createDelivery(CreateDeliveryRequest request) {
        log.info("Creating delivery for order {}", request.orderId());

        // Check if delivery already exists for this order
        if (deliveryRepository.findByOrderId(request.orderId()).isPresent()) {
            throw new InvalidDeliveryStateException(
                    "Delivery already exists for order: " + request.orderId());
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(request.orderId());
        delivery.setCustomerId(request.customerId());
        delivery.setCustomerName(request.customerName());
        delivery.setCustomerPhone(request.customerPhone());
        delivery.setPickupAddress(request.pickupAddress());
        delivery.setPickupLatitude(request.pickupLatitude());
        delivery.setPickupLongitude(request.pickupLongitude());
        delivery.setPickupInstructions(request.pickupInstructions());
        delivery.setDeliveryAddress(request.deliveryAddress());
        delivery.setDeliveryLatitude(request.deliveryLatitude());
        delivery.setDeliveryLongitude(request.deliveryLongitude());
        delivery.setDeliveryInstructions(request.deliveryInstructions());
        delivery.setDeliveryFee(request.deliveryFee());
        delivery.setPriority(request.priority() != null ? request.priority() : DeliveryPriority.NORMAL);
        delivery.setStatus(DeliveryStatus.PENDING);

        // Calculate distance and ETA
        double distanceKm = delivery.calculateTotalDistance();
        delivery.setDistanceKm(BigDecimal.valueOf(distanceKm));

        int estimatedMinutes = basePreparationMinutes + (int) (distanceKm * minutesPerKm);
        delivery.setEstimatedDurationMinutes(estimatedMinutes);
        delivery.setEstimatedPickupTime(LocalDateTime.now().plusMinutes(basePreparationMinutes));
        delivery.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(estimatedMinutes));

        delivery = deliveryRepository.save(delivery);
        log.info("Created delivery {} for order {} (distance: {:.2f} km, ETA: {} min)",
                delivery.getId(), request.orderId(), distanceKm, estimatedMinutes);

        eventProducer.sendDeliveryCreated(delivery);

        // Auto-assign courier if enabled
        if (autoAssignEnabled) {
            autoAssignCourier(delivery);
        }

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public DeliveryResponse assignCourier(UUID deliveryId, UUID courierId) {
        Delivery delivery = findDeliveryById(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new InvalidDeliveryStateException(
                    "Can only assign courier to PENDING deliveries, current status: " + delivery.getStatus());
        }

        Courier courier = courierService.findBestCourierForDelivery(
                delivery.getPickupLatitude(), delivery.getPickupLongitude());

        if (courier == null) {
            throw new InvalidDeliveryStateException("No available couriers found");
        }

        delivery.setCourier(courier);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        courier.setStatus(CourierStatus.ON_DELIVERY);

        delivery = deliveryRepository.save(delivery);
        log.info("Assigned courier {} to delivery {}", courier.getName(), deliveryId);

        eventProducer.sendCourierAssigned(delivery);

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public DeliveryResponse markPickedUp(UUID deliveryId) {
        Delivery delivery = findDeliveryWithCourier(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new InvalidDeliveryStateException(
                    "Can only pick up ASSIGNED deliveries, current status: " + delivery.getStatus());
        }

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setActualPickupTime(LocalDateTime.now());

        delivery = deliveryRepository.save(delivery);
        log.info("Delivery {} picked up by courier {}", deliveryId, delivery.getCourier().getName());

        eventProducer.sendOrderPickedUp(delivery);

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public DeliveryResponse markInTransit(UUID deliveryId) {
        Delivery delivery = findDeliveryWithCourier(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new InvalidDeliveryStateException(
                    "Can only set IN_TRANSIT after PICKED_UP, current status: " + delivery.getStatus());
        }

        delivery.setStatus(DeliveryStatus.IN_TRANSIT);
        delivery = deliveryRepository.save(delivery);
        log.info("Delivery {} is now in transit", deliveryId);

        eventProducer.sendDeliveryInTransit(delivery);

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public DeliveryResponse markArrived(UUID deliveryId) {
        Delivery delivery = findDeliveryWithCourier(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.IN_TRANSIT) {
            throw new InvalidDeliveryStateException(
                    "Can only mark arrived from IN_TRANSIT, current status: " + delivery.getStatus());
        }

        delivery.setStatus(DeliveryStatus.ARRIVED);
        delivery = deliveryRepository.save(delivery);
        log.info("Courier arrived for delivery {}", deliveryId);

        eventProducer.sendCourierArrived(delivery);

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public DeliveryResponse completeDelivery(UUID deliveryId, String deliveryNotes) {
        Delivery delivery = findDeliveryWithCourier(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.ARRIVED) {
            throw new InvalidDeliveryStateException(
                    "Can only complete ARRIVED deliveries, current status: " + delivery.getStatus());
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setActualDeliveryTime(LocalDateTime.now());
        delivery.setDeliveryNotes(deliveryNotes);

        // Update courier
        Courier courier = delivery.getCourier();
        courier.setStatus(CourierStatus.AVAILABLE);
        courierService.incrementDeliveryCount(courier.getId());

        delivery = deliveryRepository.save(delivery);
        log.info("Delivery {} completed by courier {}", deliveryId, courier.getName());

        eventProducer.sendDeliveryCompleted(delivery);

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public DeliveryResponse failDelivery(UUID deliveryId, String reason) {
        Delivery delivery = findDeliveryWithCourier(deliveryId);

        if (delivery.getStatus() == DeliveryStatus.DELIVERED ||
                delivery.getStatus() == DeliveryStatus.CANCELLED) {
            throw new InvalidDeliveryStateException(
                    "Cannot fail delivery with status: " + delivery.getStatus());
        }

        delivery.setStatus(DeliveryStatus.FAILED);
        delivery.setFailureReason(reason);
        delivery.setRetryCount(delivery.getRetryCount() + 1);

        // Release courier
        if (delivery.getCourier() != null) {
            delivery.getCourier().setStatus(CourierStatus.AVAILABLE);
        }

        delivery = deliveryRepository.save(delivery);
        log.warn("Delivery {} failed: {}", deliveryId, reason);

        eventProducer.sendDeliveryFailed(delivery, reason);

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public void updateCourierLocation(UUID deliveryId, LocationUpdateRequest request) {
        Delivery delivery = findDeliveryWithCourier(deliveryId);

        if (delivery.getCourier() == null) {
            throw new InvalidDeliveryStateException("No courier assigned to delivery");
        }

        // Update courier location
        Courier courier = delivery.getCourier();
        courier.updateLocation(request.latitude(), request.longitude());

        // Record location history
        DeliveryLocationHistory history = new DeliveryLocationHistory();
        history.setDelivery(delivery);
        history.setCourier(courier);
        history.setLatitude(request.latitude());
        history.setLongitude(request.longitude());
        history.setSpeedKmh(request.speedKmh());
        history.setHeading(request.heading());
        history.setAccuracyMeters(request.accuracyMeters());
        locationHistoryRepository.save(history);

        // Send location update event
        eventProducer.sendLocationUpdate(delivery, request.latitude(), request.longitude());
    }

    @Transactional(readOnly = true)
    public TrackingInfo getTrackingInfo(UUID deliveryId) {
        Delivery delivery = findDeliveryWithCourier(deliveryId);
        Courier courier = delivery.getCourier();

        if (courier == null) {
            return new TrackingInfo(
                    deliveryId, null, null, null, null,
                    delivery.getDeliveryLatitude(), delivery.getDeliveryLongitude(),
                    delivery.getEstimatedDurationMinutes(), delivery.getDistanceKm(),
                    delivery.getStatus().name(), LocalDateTime.now());
        }

        // Calculate remaining distance and time
        double remainingKm = courier.distanceTo(delivery.getDeliveryLatitude(), delivery.getDeliveryLongitude());
        int remainingMinutes = (int) (remainingKm * minutesPerKm);

        return new TrackingInfo(
                deliveryId,
                courier.getId(),
                courier.getName(),
                courier.getCurrentLatitude(),
                courier.getCurrentLongitude(),
                delivery.getDeliveryLatitude(),
                delivery.getDeliveryLongitude(),
                remainingMinutes,
                BigDecimal.valueOf(remainingKm),
                delivery.getStatus().name(),
                courier.getLastLocationUpdate());
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDelivery(UUID deliveryId) {
        return DeliveryResponse.from(findDeliveryWithCourier(deliveryId));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found for order: " + orderId));
        return DeliveryResponse.from(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getCourierDeliveries(UUID courierId) {
        return deliveryRepository.findByCourierId(courierId).stream()
                .map(DeliveryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DeliveryResponse> getActiveDeliveries(Pageable pageable) {
        return deliveryRepository.findAllActiveDeliveries(pageable)
                .map(DeliveryResponse::from);
    }

    private void autoAssignCourier(Delivery delivery) {
        Courier courier = courierService.findBestCourierForDelivery(
                delivery.getPickupLatitude(), delivery.getPickupLongitude());

        if (courier != null) {
            delivery.setCourier(courier);
            delivery.setStatus(DeliveryStatus.ASSIGNED);
            courier.setStatus(CourierStatus.ON_DELIVERY);
            deliveryRepository.save(delivery);
            log.info("Auto-assigned courier {} to delivery {}", courier.getName(), delivery.getId());
            eventProducer.sendCourierAssigned(delivery);
        } else {
            log.info("No available courier for auto-assignment, delivery {} remains pending", delivery.getId());
        }
    }

    private Delivery findDeliveryById(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found: " + deliveryId));
    }

    private Delivery findDeliveryWithCourier(UUID deliveryId) {
        return deliveryRepository.findByIdWithCourier(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found: " + deliveryId));
    }
}
