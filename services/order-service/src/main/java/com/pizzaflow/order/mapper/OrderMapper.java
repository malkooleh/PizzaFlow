package com.pizzaflow.order.mapper;

import com.pizzaflow.order.dto.OrderResponse;
import com.pizzaflow.order.model.Order;
import com.pizzaflow.order.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .scheduledTime(order.getScheduledTime())
                .tableNumber(order.getTableNumber())
                .reservationId(order.getReservationId())
                .deliveryAddress(order.getDeliveryAddress())
                .specialInstructions(order.getSpecialInstructions())
                .items(order.getItems().stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()))
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .confirmedAt(order.getConfirmedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }
    
    private OrderResponse.OrderItemResponse toItemResponse(OrderItem item) {
        return OrderResponse.OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .customizations(item.getCustomizations())
                .specialInstructions(item.getSpecialInstructions())
                .build();
    }
}
