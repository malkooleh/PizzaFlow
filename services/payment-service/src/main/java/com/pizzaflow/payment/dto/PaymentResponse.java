package com.pizzaflow.payment.dto;

import com.pizzaflow.payment.model.enums.PaymentMethodType;
import com.pizzaflow.payment.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID transactionId;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentMethodType paymentMethodType;
    private String gatewayTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;
}
