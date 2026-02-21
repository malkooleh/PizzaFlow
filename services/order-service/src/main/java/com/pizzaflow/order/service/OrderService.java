package com.pizzaflow.order.service;

import com.pizzaflow.order.dto.CreateOrderRequest;
import com.pizzaflow.order.dto.OrderResponse;
import com.pizzaflow.order.event.OrderCreatedEvent;
import com.pizzaflow.order.mapper.OrderMapper;
import com.pizzaflow.order.model.Order;
import com.pizzaflow.order.model.OrderItem;
import com.pizzaflow.order.model.enums.OrderStatus;
import com.pizzaflow.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderMapper orderMapper;
    
    private static final String ORDER_CREATED_TOPIC = "order.created";
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% tax
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());
        
        // Build order entity
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customerId(request.getCustomerId())
                .restaurantId(request.getRestaurantId())
                .orderType(request.getOrderType())
                .status(OrderStatus.PENDING)
                .scheduledTime(request.getScheduledTime())
                .tableNumber(request.getTableNumber())
                .reservationId(request.getReservationId())
                .deliveryAddress(request.getDeliveryAddress())
                .specialInstructions(request.getSpecialInstructions())
                .build();
        
        // Add order items
        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> {
                    BigDecimal subtotal = itemRequest.getUnitPrice()
                            .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                    
                    return OrderItem.builder()
                            .menuItemId(itemRequest.getMenuItemId())
                            .menuItemName(itemRequest.getMenuItemName())
                            .quantity(itemRequest.getQuantity())
                            .unitPrice(itemRequest.getUnitPrice())
                            .subtotal(subtotal)
                            .customizations(itemRequest.getCustomizations())
                            .specialInstructions(itemRequest.getSpecialInstructions())
                            .build();
                })
                .collect(Collectors.toList());
        
        orderItems.forEach(order::addItem);
        
        // Calculate totals
        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal tax = subtotal.multiply(TAX_RATE);
        BigDecimal deliveryFee = calculateDeliveryFee(request.getOrderType());
        BigDecimal totalAmount = subtotal.add(tax).add(deliveryFee);
        
        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(totalAmount);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        
        // Publish event
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .customerId(savedOrder.getCustomerId())
                .restaurantId(savedOrder.getRestaurantId())
                .totalAmount(savedOrder.getTotalAmount())
                .orderType(savedOrder.getOrderType().name())
                .timestamp(LocalDateTime.now())
                .build();
        
        kafkaTemplate.send(ORDER_CREATED_TOPIC, savedOrder.getId().toString(), event);
        log.info("Order created event published: {}", savedOrder.getOrderNumber());
        
        return orderMapper.toResponse(savedOrder);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        log.info("Fetching order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return orderMapper.toResponse(order);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        log.info("Fetching order by number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        return orderMapper.toResponse(order);
    }
    
    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(Long customerId) {
        log.info("Fetching orders for customer: {}", customerId);
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return orders.stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        order.setStatus(newStatus);
        
        if (newStatus == OrderStatus.CONFIRMED) {
            order.setConfirmedAt(LocalDateTime.now());
        } else if (newStatus == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }
        
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toResponse(updatedOrder);
    }
    
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);
        
        orderRepository.save(order);
    }
    
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private BigDecimal calculateDeliveryFee(com.pizzaflow.order.model.enums.OrderType orderType) {
        return switch (orderType) {
            case DELIVERY, SCHEDULED -> new BigDecimal("5.00");
            default -> BigDecimal.ZERO;
        };
    }
}
