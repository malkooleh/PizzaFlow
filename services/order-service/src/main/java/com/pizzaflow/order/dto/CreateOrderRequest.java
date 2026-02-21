package com.pizzaflow.order.dto;

import com.pizzaflow.order.model.enums.OrderType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
    
    @NotNull(message = "Order type is required")
    private OrderType orderType;
    
    private LocalDateTime scheduledTime;
    
    private String tableNumber;
    
    private Long reservationId;
    
    @Size(max = 500, message = "Delivery address must not exceed 500 characters")
    private String deliveryAddress;
    
    @Size(max = 1000, message = "Special instructions must not exceed 1000 characters")
    private String specialInstructions;
    
    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        
        @NotBlank(message = "Menu item ID is required")
        private String menuItemId;
        
        @NotBlank(message = "Menu item name is required")
        private String menuItemName;
        
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        @DecimalMin(value = "0.0", message = "Unit price must be positive")
        private BigDecimal unitPrice;
        
        private String customizations;
        
        private String specialInstructions;
    }
}
