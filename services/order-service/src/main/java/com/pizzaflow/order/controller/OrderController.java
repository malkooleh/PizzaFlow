package com.pizzaflow.order.controller;

import com.pizzaflow.common.dto.ApiResponse;
import com.pizzaflow.order.dto.CreateOrderRequest;
import com.pizzaflow.order.dto.OrderResponse;
import com.pizzaflow.order.model.enums.OrderStatus;
import com.pizzaflow.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Order created successfully"));
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @PathVariable String orderNumber) {
        OrderResponse response = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getCustomerOrders(
            @PathVariable Long customerId) {
        List<OrderResponse> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Order status updated"));
    }
    
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Order cancelled successfully"));
    }
}
