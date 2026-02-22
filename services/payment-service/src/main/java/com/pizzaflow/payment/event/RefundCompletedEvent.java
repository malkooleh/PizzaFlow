package com.pizzaflow.payment.event;

import com.pizzaflow.common.dto.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published when a refund is completed.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RefundCompletedEvent extends BaseEvent {

    private UUID refundId;
    private UUID transactionId;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String reason;
}
