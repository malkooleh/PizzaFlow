package com.pizzaflow.order.event;

import com.pizzaflow.common.dto.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends BaseEvent {
    
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private Long restaurantId;
    private BigDecimal totalAmount;
    private String orderType;
}
