package com.pizzaflow.payment.mapper;

import com.pizzaflow.payment.dto.PaymentMethodDTO;
import com.pizzaflow.payment.dto.PaymentResponse;
import com.pizzaflow.payment.dto.RefundResponse;
import com.pizzaflow.payment.model.PaymentMethod;
import com.pizzaflow.payment.model.Refund;
import com.pizzaflow.payment.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toPaymentResponse(Transaction transaction) {
        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .orderId(transaction.getOrderId())
                .customerId(transaction.getCustomerId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .paymentMethodType(transaction.getPaymentMethodType())
                .gatewayTransactionId(transaction.getGatewayTransactionId())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .failureReason(transaction.getFailureReason())
                .build();
    }

    public RefundResponse toRefundResponse(Refund refund) {
        return RefundResponse.builder()
                .refundId(refund.getId())
                .transactionId(refund.getTransaction().getId())
                .orderId(refund.getOrderId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .completedAt(refund.getCompletedAt())
                .build();
    }

    public PaymentMethodDTO toPaymentMethodDTO(PaymentMethod paymentMethod) {
        return PaymentMethodDTO.builder()
                .id(paymentMethod.getId())
                .customerId(paymentMethod.getCustomerId())
                .type(paymentMethod.getType())
                .cardLastFour(paymentMethod.getCardLastFour())
                .cardBrand(paymentMethod.getCardBrand())
                .cardExpiryMonth(paymentMethod.getCardExpiryMonth())
                .cardExpiryYear(paymentMethod.getCardExpiryYear())
                .isDefault(paymentMethod.getIsDefault())
                .isActive(paymentMethod.getIsActive())
                .createdAt(paymentMethod.getCreatedAt())
                .build();
    }
}
