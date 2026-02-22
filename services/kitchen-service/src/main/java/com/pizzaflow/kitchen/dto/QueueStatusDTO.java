package com.pizzaflow.kitchen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusDTO {

    private Long restaurantId;
    private Integer totalOrders;
    private Integer receivedCount;
    private Integer preparingCount;
    private Integer readyCount;
    private Integer averageWaitTimeMinutes;
    private List<KitchenOrderDTO> orders;
}
