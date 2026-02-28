package com.pizzaflow.order.service;

import com.pizzaflow.order.dto.CreateOrderRequest;
import com.pizzaflow.order.dto.CreateOrderRequest.OrderItemRequest;
import com.pizzaflow.order.dto.OrderResponse;
import com.pizzaflow.order.exception.OrderNotFoundException;
import com.pizzaflow.order.mapper.OrderMapper;
import com.pizzaflow.order.model.Order;
import com.pizzaflow.order.model.enums.OrderStatus;
import com.pizzaflow.order.model.enums.OrderType;
import com.pizzaflow.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;
    private OrderResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-ABCD1234")
                .customerId(100L)
                .restaurantId(1L)
                .orderType(OrderType.DINE_IN)
                .status(OrderStatus.PENDING)
                .subtotal(new BigDecimal("20.00"))
                .tax(new BigDecimal("1.60"))
                .deliveryFee(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("21.60"))
                .build();

        sampleResponse = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-ABCD1234")
                .customerId(100L)
                .status(OrderStatus.PENDING)
                .build();
    }

    // ── createOrder ───────────────────────────────────────────────────────────

    @Test
    void createOrder_shouldPersistOrderAndPublishEvent() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(100L)
                .restaurantId(1L)
                .orderType(OrderType.DINE_IN)
                .items(List.of(OrderItemRequest.builder()
                        .menuItemId("item-1")
                        .menuItemName("Margherita Pizza")
                        .quantity(2)
                        .unitPrice(new BigDecimal("10.00"))
                        .build()))
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(orderMapper.toResponse(sampleOrder)).thenReturn(sampleResponse);

        OrderResponse result = orderService.createOrder(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void createOrder_shouldCalculateDeliveryFeeForDeliveryOrder() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(100L)
                .restaurantId(1L)
                .orderType(OrderType.DELIVERY)
                .deliveryAddress("123 Main St")
                .items(List.of(OrderItemRequest.builder()
                        .menuItemId("item-1")
                        .menuItemName("Pepperoni Pizza")
                        .quantity(1)
                        .unitPrice(new BigDecimal("15.00"))
                        .build()))
                .build();

        Order deliveryOrder = Order.builder()
                .id(2L)
                .orderNumber("ORD-XYZ")
                .customerId(100L)
                .orderType(OrderType.DELIVERY)
                .status(OrderStatus.PENDING)
                .deliveryFee(new BigDecimal("5.00"))
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(deliveryOrder);
        when(orderMapper.toResponse(deliveryOrder)).thenReturn(
                OrderResponse.builder().id(2L).orderNumber("ORD-XYZ").build());

        OrderResponse result = orderService.createOrder(request);

        assertThat(result).isNotNull();
        // Delivery fee is 5.00 for DELIVERY type — verified via saved entity
        verify(orderRepository).save(argThat(o -> o.getDeliveryFee().compareTo(new BigDecimal("5.00")) == 0));
    }

    @Test
    void createOrder_shouldUseZeroDeliveryFeeForDineIn() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(100L)
                .restaurantId(1L)
                .orderType(OrderType.DINE_IN)
                .items(List.of(OrderItemRequest.builder()
                        .menuItemId("item-1")
                        .menuItemName("Margherita")
                        .quantity(1)
                        .unitPrice(new BigDecimal("12.00"))
                        .build()))
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        when(orderMapper.toResponse(any())).thenReturn(sampleResponse);

        orderService.createOrder(request);

        verify(orderRepository).save(argThat(o -> o.getDeliveryFee().compareTo(BigDecimal.ZERO) == 0));
    }

    // ── getOrder ──────────────────────────────────────────────────────────────

    @Test
    void getOrder_shouldReturnOrderResponse_whenOrderExists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderMapper.toResponse(sampleOrder)).thenReturn(sampleResponse);

        OrderResponse result = orderService.getOrder(1L);

        assertThat(result).isEqualTo(sampleResponse);
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrder_shouldThrowOrderNotFoundException_whenOrderDoesNotExist() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getOrderByNumber ──────────────────────────────────────────────────────

    @Test
    void getOrderByNumber_shouldReturnOrderResponse_whenOrderExists() {
        when(orderRepository.findByOrderNumber("ORD-ABCD1234")).thenReturn(Optional.of(sampleOrder));
        when(orderMapper.toResponse(sampleOrder)).thenReturn(sampleResponse);

        OrderResponse result = orderService.getOrderByNumber("ORD-ABCD1234");

        assertThat(result).isEqualTo(sampleResponse);
    }

    @Test
    void getOrderByNumber_shouldThrowOrderNotFoundException_whenOrderNotFound() {
        when(orderRepository.findByOrderNumber("ORD-INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderByNumber("ORD-INVALID"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("ORD-INVALID");
    }

    // ── updateOrderStatus ─────────────────────────────────────────────────────

    @Test
    void updateOrderStatus_shouldUpdateStatusAndSave() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        Order confirmed = Order.builder()
                .id(1L).orderNumber("ORD-ABCD1234")
                .customerId(100L).orderType(OrderType.DINE_IN)
                .status(OrderStatus.CONFIRMED).build();
        when(orderRepository.save(sampleOrder)).thenReturn(confirmed);
        when(orderMapper.toResponse(confirmed)).thenReturn(
                OrderResponse.builder().id(1L).status(OrderStatus.CONFIRMED).build());

        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(sampleOrder);
    }

    @Test
    void updateOrderStatus_shouldThrowOrderNotFoundException_whenOrderMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(99L, OrderStatus.CONFIRMED))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ── cancelOrder ───────────────────────────────────────────────────────────

    @Test
    void cancelOrder_shouldSetStatusToCancelled() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(sampleOrder)).thenReturn(sampleOrder);

        orderService.cancelOrder(1L, "Customer request");

        assertThat(sampleOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(sampleOrder.getCancellationReason()).isEqualTo("Customer request");
        verify(orderRepository).save(sampleOrder);
    }

    @Test
    void cancelOrder_shouldThrowOrderNotFoundException_whenOrderMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(99L, "reason"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ── getCustomerOrders ─────────────────────────────────────────────────────

    @Test
    void getCustomerOrders_shouldReturnEmptyList_whenNoOrders() {
        when(orderRepository.findByCustomerIdOrderByCreatedAtDesc(100L)).thenReturn(List.of());

        List<OrderResponse> result = orderService.getCustomerOrders(100L);

        assertThat(result).isEmpty();
    }

    @Test
    void getCustomerOrders_shouldReturnMappedOrders() {
        when(orderRepository.findByCustomerIdOrderByCreatedAtDesc(100L)).thenReturn(List.of(sampleOrder));
        when(orderMapper.toResponse(sampleOrder)).thenReturn(sampleResponse);

        List<OrderResponse> result = orderService.getCustomerOrders(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(sampleResponse);
    }
}
