package com.pizzaflow.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaflow.inventory.exception.InventoryItemNotFoundException;
import com.pizzaflow.inventory.dto.*;
import com.pizzaflow.inventory.event.*;
import com.pizzaflow.inventory.mapper.InventoryMapper;
import com.pizzaflow.inventory.model.*;
import com.pizzaflow.inventory.model.enums.AggregateType;
import com.pizzaflow.inventory.model.enums.ReservationStatus;
import com.pizzaflow.inventory.repository.*;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed(name = "inventory.service", contextualName = "inventory-service")
public class InventoryService {

    private final IngredientRepository ingredientRepository;
    private final StockLevelRepository stockLevelRepository;
    private final ReservationRepository reservationRepository;
    private final OutboxRepository outboxRepository;
    private final InventoryMapper inventoryMapper;
    private final ObjectMapper objectMapper;

    /**
     * Reserve ingredients for an order.
     * Uses the Transactional Outbox pattern for reliable event publishing.
     */
    @Transactional
    public ReservationResponse reserveIngredients(ReservationRequest request) {
        log.info("Reserving ingredients for order: {}", request.getOrderId());

        List<ReservationItemResponse> itemResponses = new ArrayList<>();
        List<Reservation> successfulReservations = new ArrayList<>();
        List<InventoryReservationFailedEvent.FailedItem> failedItems = new ArrayList<>();

        for (ReservationItemRequest item : request.getItems()) {
            StockLevel stockLevel = stockLevelRepository.findByIngredientIdAndRestaurantId(
                    item.getIngredientId(), request.getRestaurantId()).orElse(null);

            if (stockLevel == null) {
                log.warn("Stock level not found for ingredient: {} at restaurant: {}",
                        item.getIngredientId(), request.getRestaurantId());
                Ingredient ingredient = ingredientRepository.findById(item.getIngredientId()).orElse(null);
                failedItems.add(InventoryReservationFailedEvent.FailedItem.builder()
                        .ingredientId(item.getIngredientId())
                        .ingredientName(ingredient != null ? ingredient.getName() : "Unknown")
                        .requestedQuantity(item.getQuantity().toString())
                        .availableQuantity("0")
                        .build());
                continue;
            }

            // Check if sufficient stock is available
            BigDecimal available = stockLevel.getAvailableQuantity();
            if (available.compareTo(item.getQuantity()) < 0) {
                log.warn("Insufficient stock for ingredient: {}, required: {}, available: {}",
                        stockLevel.getIngredient().getName(), item.getQuantity(), available);
                failedItems.add(InventoryReservationFailedEvent.FailedItem.builder()
                        .ingredientId(item.getIngredientId())
                        .ingredientName(stockLevel.getIngredient().getName())
                        .requestedQuantity(item.getQuantity().toString())
                        .availableQuantity(available.toString())
                        .build());
                continue;
            }

            // Reserve the stock
            int updated = stockLevelRepository.reserveStock(stockLevel.getId(), item.getQuantity());
            if (updated == 0) {
                log.warn("Failed to reserve stock due to concurrent update for ingredient: {}",
                        stockLevel.getIngredient().getName());
                failedItems.add(InventoryReservationFailedEvent.FailedItem.builder()
                        .ingredientId(item.getIngredientId())
                        .ingredientName(stockLevel.getIngredient().getName())
                        .requestedQuantity(item.getQuantity().toString())
                        .availableQuantity(available.toString())
                        .build());
                continue;
            }

            // Create reservation record
            Reservation reservation = Reservation.builder()
                    .orderId(request.getOrderId())
                    .ingredient(stockLevel.getIngredient())
                    .restaurantId(request.getRestaurantId())
                    .quantity(item.getQuantity())
                    .status(ReservationStatus.PENDING)
                    .build();
            reservation = reservationRepository.save(reservation);
            successfulReservations.add(reservation);

            // Refresh stock level to get updated values
            stockLevel = stockLevelRepository.findById(stockLevel.getId()).orElse(stockLevel);

            itemResponses.add(inventoryMapper.toReservationItemResponse(reservation, stockLevel));

            // Check for low stock alert
            checkAndPublishLowStockAlert(stockLevel);
        }

        // Determine overall status
        ReservationStatus overallStatus;
        String message;
        if (failedItems.isEmpty()) {
            overallStatus = ReservationStatus.CONFIRMED;
            message = "All ingredients reserved successfully";
            confirmReservations(successfulReservations);
            publishInventoryReservedEvent(request, successfulReservations);
        } else if (successfulReservations.isEmpty()) {
            overallStatus = ReservationStatus.RELEASED;
            message = "Failed to reserve any ingredients";
            publishInventoryReservationFailedEvent(request, failedItems, "Insufficient stock for all items");
        } else {
            // Partial success - rollback successful reservations
            overallStatus = ReservationStatus.RELEASED;
            message = "Partial reservation failure - rolling back";
            releaseReservationsInternal(successfulReservations);
            publishInventoryReservationFailedEvent(request, failedItems, "Partial reservation failure");
        }

        return ReservationResponse.builder()
                .orderId(request.getOrderId())
                .restaurantId(request.getRestaurantId())
                .status(overallStatus)
                .items(itemResponses)
                .reservedAt(LocalDateTime.now())
                .message(message)
                .build();
    }

    /**
     * Consume reserved ingredients when order preparation starts.
     */
    @Transactional
    public void consumeReservedIngredients(UUID orderId) {
        log.info("Consuming reserved ingredients for order: {}", orderId);

        List<Reservation> reservations = reservationRepository.findByOrderIdAndStatus(
                orderId, ReservationStatus.CONFIRMED);

        if (reservations.isEmpty()) {
            log.warn("No confirmed reservations found for order: {}", orderId);
            return;
        }

        UUID restaurantId = reservations.get(0).getRestaurantId();

        for (Reservation reservation : reservations) {
            StockLevel stockLevel = stockLevelRepository.findByIngredientIdAndRestaurantId(
                    reservation.getIngredient().getId(), reservation.getRestaurantId()).orElse(null);

            if (stockLevel != null) {
                int updated = stockLevelRepository.consumeReservedStock(
                        stockLevel.getId(), reservation.getQuantity());

                if (updated == 0) {
                    log.error("Failed to consume stock for reservation: {}", reservation.getId());
                    continue;
                }

                // Check for low stock alert after consumption
                stockLevel = stockLevelRepository.findById(stockLevel.getId()).orElse(stockLevel);
                checkAndPublishLowStockAlert(stockLevel);
            }
        }

        // Publish inventory consumed event
        publishInventoryConsumedEvent(orderId, restaurantId);

        log.info("Successfully consumed ingredients for order: {}", orderId);
    }

    /**
     * Release reservations when an order is cancelled.
     */
    @Transactional
    public void releaseReservations(UUID orderId) {
        log.info("Releasing reservations for order: {}", orderId);

        List<Reservation> reservations = reservationRepository.findByOrderIdWithIngredient(orderId);

        if (reservations.isEmpty()) {
            log.warn("No reservations found for order: {}", orderId);
            return;
        }

        UUID restaurantId = reservations.get(0).getRestaurantId();
        releaseReservationsInternal(reservations);
        publishInventoryReleasedEvent(orderId, restaurantId);
    }

    private void releaseReservationsInternal(List<Reservation> reservations) {
        for (Reservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.PENDING ||
                    reservation.getStatus() == ReservationStatus.CONFIRMED) {

                StockLevel stockLevel = stockLevelRepository.findByIngredientIdAndRestaurantId(
                        reservation.getIngredient().getId(), reservation.getRestaurantId()).orElse(null);

                if (stockLevel != null) {
                    stockLevelRepository.releaseReservedStock(stockLevel.getId(), reservation.getQuantity());
                }

                reservation.setStatus(ReservationStatus.RELEASED);
                reservation.setReleasedAt(LocalDateTime.now());
                reservationRepository.save(reservation);
            }
        }
    }

    private void confirmReservations(List<Reservation> reservations) {
        for (Reservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setConfirmedAt(LocalDateTime.now());
            reservationRepository.save(reservation);
        }
    }

    /**
     * Get stock levels for a restaurant.
     */
    @Transactional(readOnly = true)
    public List<StockLevelDTO> getStockLevels(UUID restaurantId) {
        return stockLevelRepository.findByRestaurantIdWithIngredient(restaurantId)
                .stream()
                .map(inventoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get low stock items for a restaurant.
     */
    @Transactional(readOnly = true)
    public List<StockLevelDTO> getLowStockItems(UUID restaurantId) {
        return stockLevelRepository.findLowStockByRestaurant(restaurantId)
                .stream()
                .map(inventoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Adjust stock level (restock or adjustment).
     */
    @Transactional
    public StockLevelDTO adjustStock(StockAdjustmentRequest request) {
        log.info("Adjusting stock for ingredient: {} at restaurant: {}, quantity: {}",
                request.getIngredientId(), request.getRestaurantId(), request.getQuantity());

        StockLevel stockLevel = stockLevelRepository.findByIngredientIdAndRestaurantId(
                request.getIngredientId(), request.getRestaurantId()).orElseThrow(
                        () -> new InventoryItemNotFoundException(
                                request.getIngredientId().toString()));

        stockLevel.setCurrentQuantity(stockLevel.getCurrentQuantity().add(request.getQuantity()));
        stockLevel.setLastRestockedAt(LocalDateTime.now());
        stockLevel = stockLevelRepository.save(stockLevel);

        return inventoryMapper.toDTO(stockLevel);
    }

    /**
     * Get all ingredients.
     */
    @Transactional(readOnly = true)
    public List<IngredientDTO> getAllIngredients() {
        return ingredientRepository.findByIsActiveTrue()
                .stream()
                .map(inventoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Event publishing methods using Transactional Outbox pattern

    private void publishInventoryReservedEvent(ReservationRequest request, List<Reservation> reservations) {
        InventoryReservedEvent event = InventoryReservedEvent.builder()
                .orderId(request.getOrderId())
                .restaurantId(request.getRestaurantId())
                .reservedAt(LocalDateTime.now())
                .items(reservations.stream()
                        .map(r -> InventoryReservedEvent.ReservedItem.builder()
                                .ingredientId(r.getIngredient().getId())
                                .ingredientName(r.getIngredient().getName())
                                .quantity(r.getQuantity().toString())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        saveOutboxEvent(request.getOrderId().toString(), AggregateType.RESERVATION,
                "INVENTORY_RESERVED", event);
    }

    private void publishInventoryReservationFailedEvent(ReservationRequest request,
            List<InventoryReservationFailedEvent.FailedItem> failedItems, String reason) {

        InventoryReservationFailedEvent event = InventoryReservationFailedEvent.builder()
                .orderId(request.getOrderId())
                .restaurantId(request.getRestaurantId())
                .failedItems(failedItems)
                .reason(reason)
                .failedAt(LocalDateTime.now())
                .build();

        saveOutboxEvent(request.getOrderId().toString(), AggregateType.RESERVATION,
                "INVENTORY_RESERVATION_FAILED", event);
    }

    private void publishInventoryConsumedEvent(UUID orderId, UUID restaurantId) {
        InventoryConsumedEvent event = InventoryConsumedEvent.builder()
                .orderId(orderId)
                .restaurantId(restaurantId)
                .consumedAt(LocalDateTime.now())
                .build();

        saveOutboxEvent(orderId.toString(), AggregateType.RESERVATION,
                "INVENTORY_CONSUMED", event);
    }

    private void publishInventoryReleasedEvent(UUID orderId, UUID restaurantId) {
        InventoryReleasedEvent event = InventoryReleasedEvent.builder()
                .orderId(orderId)
                .restaurantId(restaurantId)
                .releasedAt(LocalDateTime.now())
                .build();

        saveOutboxEvent(orderId.toString(), AggregateType.RESERVATION,
                "INVENTORY_RELEASED", event);
    }

    private void checkAndPublishLowStockAlert(StockLevel stockLevel) {
        if (stockLevel.isLowStock()) {
            Ingredient ingredient = stockLevel.getIngredient();
            LowStockAlertEvent event = LowStockAlertEvent.builder()
                    .ingredientId(ingredient.getId())
                    .ingredientName(ingredient.getName())
                    .restaurantId(stockLevel.getRestaurantId())
                    .currentQuantity(stockLevel.getAvailableQuantity().toString())
                    .minimumStockLevel(ingredient.getMinimumStockLevel().toString())
                    .reorderQuantity(ingredient.getReorderQuantity().toString())
                    .alertedAt(LocalDateTime.now())
                    .build();

            saveOutboxEvent(ingredient.getId().toString(), AggregateType.STOCK_LEVEL,
                    "LOW_STOCK_ALERT", event);
        }
    }

    private void saveOutboxEvent(String aggregateId, AggregateType aggregateType,
            String eventType, Object payload) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .aggregateType(aggregateType)
                    .eventType(eventType)
                    .payload(objectMapper.writeValueAsString(payload))
                    .build();
            outboxRepository.save(outboxEvent);
            log.debug("Saved outbox event: type={}, aggregateId={}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event payload: {}", e.getMessage());
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
}
