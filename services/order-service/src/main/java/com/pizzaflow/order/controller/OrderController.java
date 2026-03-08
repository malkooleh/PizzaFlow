package com.pizzaflow.order.controller;

import com.pizzaflow.common.dto.ApiResponse;
import com.pizzaflow.order.dto.CreateOrderRequest;
import com.pizzaflow.order.dto.OrderResponse;
import com.pizzaflow.order.model.enums.OrderStatus;
import com.pizzaflow.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders V1", description = "Standard order lifecycle management (create, read, update, cancel)")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Creates an order and triggers inventory reservation + payment events")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Order created successfully"));
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get order by order number")
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @PathVariable String orderNumber) {
        OrderResponse response = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get all orders for a customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getCustomerOrders(
            @PathVariable Long customerId,
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> orders = orderService.getCustomerOrders(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @Operation(summary = "Update order status")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Order status updated"));
    }

    @Operation(summary = "Cancel an order", description = "Cancels the order and releases any inventory reservations")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Order cancelled successfully"));
    }
}
