package com.pizzaflow.inventory.dto;

import com.pizzaflow.inventory.model.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private UUID orderId;
    private UUID restaurantId;
    private ReservationStatus status;
    private List<ReservationItemResponse> items;
    private LocalDateTime reservedAt;
    private String message;
}
