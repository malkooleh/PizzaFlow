package com.pizzaflow.payment.dto;

import com.pizzaflow.payment.model.enums.PaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDTO {

    private UUID id;
    private Long customerId;
    private PaymentMethodType type;
    private String cardLastFour;
    private String cardBrand;
    private Integer cardExpiryMonth;
    private Integer cardExpiryYear;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
