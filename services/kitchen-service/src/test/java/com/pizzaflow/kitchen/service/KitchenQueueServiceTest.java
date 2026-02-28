package com.pizzaflow.kitchen.service;

import com.pizzaflow.kitchen.dto.KitchenOrderDTO;
import com.pizzaflow.kitchen.event.PaymentCompletedEvent;
import com.pizzaflow.kitchen.exception.InvalidKitchenOrderStateException;
import com.pizzaflow.kitchen.exception.KitchenOrderNotFoundException;
import com.pizzaflow.kitchen.model.KitchenOrder;
import com.pizzaflow.kitchen.model.enums.KitchenOrderStatus;
import com.pizzaflow.kitchen.model.enums.OrderPriority;
import com.pizzaflow.kitchen.repository.KitchenOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KitchenQueueServiceTest {

    @Mock
    private KitchenOrderRepository kitchenOrderRepository;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private KitchenQueueService kitchenQueueService;

    private KitchenOrder receivedOrder;
    private KitchenOrder preparingOrder;

    @BeforeEach
    void setUp() {
        receivedOrder = KitchenOrder.builder()
                .id("kitchen-order-1")
                .orderId(1001L)
                .orderNumber("ORD-ABCD1234")
                .restaurantId(1L)
                .customerId(100L)
                .orderType("DINE_IN")
                .status(KitchenOrderStatus.RECEIVED)
                .priority(OrderPriority.NORMAL)
                .estimatedPrepTimeMinutes(20)
                .queuePosition(1)
                .build();

        preparingOrder = KitchenOrder.builder()
                .id("kitchen-order-1")
                .orderId(1001L)
                .orderNumber("ORD-ABCD1234")
                .restaurantId(1L)
                .status(KitchenOrderStatus.PREPARING)
                .priority(OrderPriority.NORMAL)
                .estimatedPrepTimeMinutes(20)
                .build();
    }

    // ── addOrderToQueue ───────────────────────────────────────────────────────

    @Test
    void addOrderToQueue_shouldCreateNewKitchenOrder_whenOrderDoesNotExist() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(1001L)
                .orderNumber("ORD-ABCD1234")
                .restaurantId(1L)
                .customerId(100L)
                .orderType("DINE_IN")
                .items(List.of())
                .build();

        when(kitchenOrderRepository.existsByOrderId(1001L)).thenReturn(false);
        when(kitchenOrderRepository.save(any(KitchenOrder.class))).thenReturn(receivedOrder);
        when(kitchenOrderRepository.findByRestaurantIdAndStatus(any(), any())).thenReturn(List.of());

        KitchenOrderDTO result = kitchenQueueService.addOrderToQueue(event);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1001L);
        verify(kitchenOrderRepository).save(any(KitchenOrder.class));
        verify(webSocketService).notifyNewOrder(any(), any(), any(), any(), any());
    }

    @Test
    void addOrderToQueue_shouldReturnExistingOrder_whenOrderAlreadyExistsInQueue() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(1001L)
                .orderNumber("ORD-ABCD1234")
                .restaurantId(1L)
                .customerId(100L)
                .orderType("DINE_IN")
                .items(List.of())
                .build();

        when(kitchenOrderRepository.existsByOrderId(1001L)).thenReturn(true);
        when(kitchenOrderRepository.findByOrderId(1001L)).thenReturn(Optional.of(receivedOrder));

        KitchenOrderDTO result = kitchenQueueService.addOrderToQueue(event);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1001L);
        // Should NOT save again — idempotency guard
        verify(kitchenOrderRepository, never()).save(any());
    }

    // ── startPreparing ────────────────────────────────────────────────────────

    @Test
    void startPreparing_shouldTransitionToPreparingStatus() {
        when(kitchenOrderRepository.findByOrderId(1001L)).thenReturn(Optional.of(receivedOrder));
        when(kitchenOrderRepository.save(receivedOrder)).thenReturn(preparingOrder);
        when(kitchenOrderRepository.findByRestaurantIdAndStatus(any(), any())).thenReturn(List.of());

        KitchenOrderDTO result = kitchenQueueService.startPreparing(1001L, "STATION-1");

        assertThat(result.getStatus()).isEqualTo(KitchenOrderStatus.PREPARING);
        verify(kafkaProducerService).publishOrderPreparing(any(), any(), any(), any(), any());
        verify(webSocketService).notifyStatusChange(any(), any(), any(), any(), any());
    }

    @Test
    void startPreparing_shouldThrowKitchenOrderNotFoundException_whenOrderNotFound() {
        when(kitchenOrderRepository.findByOrderId(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kitchenQueueService.startPreparing(9999L, "STATION-1"))
                .isInstanceOf(KitchenOrderNotFoundException.class)
                .hasMessageContaining("9999");
    }

    @Test
    void startPreparing_shouldThrowInvalidKitchenOrderStateException_whenOrderNotInReceivedState() {
        when(kitchenOrderRepository.findByOrderId(1001L)).thenReturn(Optional.of(preparingOrder));

        assertThatThrownBy(() -> kitchenQueueService.startPreparing(1001L, "STATION-1"))
                .isInstanceOf(InvalidKitchenOrderStateException.class)
                .hasMessageContaining("PREPARING");
    }

    // ── markReady ─────────────────────────────────────────────────────────────

    @Test
    void markReady_shouldTransitionToReadyStatus() {
        preparingOrder.setStartedAt(java.time.LocalDateTime.now().minusMinutes(15));
        when(kitchenOrderRepository.findByOrderId(1001L)).thenReturn(Optional.of(preparingOrder));
        KitchenOrder readyOrder = KitchenOrder.builder()
                .id("kitchen-order-1").orderId(1001L).orderNumber("ORD-ABCD1234")
                .restaurantId(1L).status(KitchenOrderStatus.READY).build();
        when(kitchenOrderRepository.save(preparingOrder)).thenReturn(readyOrder);

        KitchenOrderDTO result = kitchenQueueService.markReady(1001L);

        assertThat(result.getStatus()).isEqualTo(KitchenOrderStatus.READY);
        verify(kafkaProducerService).publishOrderReady(any(), any(), any(), any(), any(), any());
    }

    @Test
    void markReady_shouldThrowInvalidKitchenOrderStateException_whenOrderNotPreparing() {
        when(kitchenOrderRepository.findByOrderId(1001L)).thenReturn(Optional.of(receivedOrder));

        assertThatThrownBy(() -> kitchenQueueService.markReady(1001L))
                .isInstanceOf(InvalidKitchenOrderStateException.class)
                .hasMessageContaining("RECEIVED");
    }

    // ── markPickedUp ──────────────────────────────────────────────────────────

    @Test
    void markPickedUp_shouldThrowInvalidKitchenOrderStateException_whenOrderNotReady() {
        when(kitchenOrderRepository.findByOrderId(1001L)).thenReturn(Optional.of(receivedOrder));

        assertThatThrownBy(() -> kitchenQueueService.markPickedUp(1001L))
                .isInstanceOf(InvalidKitchenOrderStateException.class)
                .hasMessageContaining("RECEIVED");
    }

    @Test
    void markPickedUp_shouldThrowKitchenOrderNotFoundException_whenOrderNotFound() {
        when(kitchenOrderRepository.findByOrderId(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kitchenQueueService.markPickedUp(9999L))
                .isInstanceOf(KitchenOrderNotFoundException.class);
    }

    // ── getKitchenOrder ───────────────────────────────────────────────────────

    @Test
    void getKitchenOrder_shouldReturnDTO_whenOrderExists() {
        when(kitchenOrderRepository.findByOrderId(1001L)).thenReturn(Optional.of(receivedOrder));

        KitchenOrderDTO result = kitchenQueueService.getKitchenOrder(1001L);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1001L);
    }

    @Test
    void getKitchenOrder_shouldThrowKitchenOrderNotFoundException_whenOrderNotFound() {
        when(kitchenOrderRepository.findByOrderId(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kitchenQueueService.getKitchenOrder(9999L))
                .isInstanceOf(KitchenOrderNotFoundException.class)
                .hasMessageContaining("9999");
    }
}
