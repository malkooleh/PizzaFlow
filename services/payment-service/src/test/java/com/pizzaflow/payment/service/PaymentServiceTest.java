package com.pizzaflow.payment.service;

import com.pizzaflow.payment.dto.PaymentRequest;
import com.pizzaflow.payment.dto.PaymentResponse;
import com.pizzaflow.payment.exception.DuplicatePaymentException;
import com.pizzaflow.payment.exception.PaymentNotFoundException;
import com.pizzaflow.payment.mapper.PaymentMapper;
import com.pizzaflow.payment.model.Transaction;
import com.pizzaflow.payment.model.enums.PaymentMethodType;
import com.pizzaflow.payment.model.enums.PaymentStatus;
import com.pizzaflow.payment.repository.PaymentMethodRepository;
import com.pizzaflow.payment.repository.RefundRepository;
import com.pizzaflow.payment.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private Transaction sampleTransaction;
    private PaymentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .orderId(1001L)
                .customerId(100L)
                .amount(new BigDecimal("25.00"))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .paymentMethodType(PaymentMethodType.CREDIT_CARD)
                .build();

        sampleResponse = PaymentResponse.builder()
                .transactionId(sampleTransaction.getId())
                .orderId(1001L)
                .amount(new BigDecimal("25.00"))
                .status(PaymentStatus.COMPLETED)
                .build();
    }

    // ── processPayment ───────────────────────────────────────────────────────

    @Test
    void processPayment_shouldThrowDuplicatePaymentException_whenPaymentAlreadyExists() {
        when(transactionRepository.existsByOrderId(1001L)).thenReturn(true);

        PaymentRequest request = PaymentRequest.builder()
                .orderId(1001L)
                .customerId(100L)
                .amount(new BigDecimal("25.00"))
                .paymentMethodType(PaymentMethodType.CREDIT_CARD)
                .build();

        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(DuplicatePaymentException.class)
                .hasMessageContaining("1001");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processPayment_shouldCompletePayment_whenGatewaySucceeds() {
        when(transactionRepository.existsByOrderId(1001L)).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);
        when(paymentGatewayService.processPayment(any(), any(), any(), any()))
                .thenReturn(PaymentGatewayService.GatewayResponse.builder()
                        .success(true)
                        .gatewayName("STRIPE_SIMULATOR")
                        .gatewayTransactionId("TXN-" + UUID.randomUUID())
                        .message("Approved")
                        .build());
        when(paymentMapper.toPaymentResponse(any())).thenReturn(sampleResponse);

        PaymentRequest request = PaymentRequest.builder()
                .orderId(1001L)
                .customerId(100L)
                .amount(new BigDecimal("25.00"))
                .paymentMethodType(PaymentMethodType.CREDIT_CARD)
                .cardNumber("4111111111111111")
                .build();

        PaymentResponse result = paymentService.processPayment(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void processPayment_shouldSetStatusFailed_whenGatewayFails() {
        when(transactionRepository.existsByOrderId(1001L)).thenReturn(false);
        when(paymentGatewayService.processPayment(any(), any(), any(), any()))
                .thenReturn(PaymentGatewayService.GatewayResponse.builder()
                        .success(false)
                        .gatewayName("STRIPE_SIMULATOR")
                        .message("Card declined")
                        .errorCode("INSUFFICIENT_FUNDS")
                        .build());

        Transaction failedTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .orderId(1001L)
                .customerId(100L)
                .amount(new BigDecimal("25.00"))
                .status(PaymentStatus.FAILED)
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(failedTransaction);
        when(paymentMapper.toPaymentResponse(any())).thenReturn(
                PaymentResponse.builder()
                        .orderId(1001L)
                        .status(PaymentStatus.FAILED)
                        .build());

        PaymentRequest request = PaymentRequest.builder()
                .orderId(1001L)
                .customerId(100L)
                .amount(new BigDecimal("25.00"))
                .paymentMethodType(PaymentMethodType.CREDIT_CARD)
                .build();

        PaymentResponse result = paymentService.processPayment(request);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    // ── getPayment ───────────────────────────────────────────────────────────

    @Test
    void getPayment_shouldReturnResponse_whenTransactionExists() {
        UUID transactionId = sampleTransaction.getId();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(sampleTransaction));
        when(paymentMapper.toPaymentResponse(sampleTransaction)).thenReturn(sampleResponse);

        PaymentResponse result = paymentService.getPayment(transactionId);

        assertThat(result).isEqualTo(sampleResponse);
    }

    @Test
    void getPayment_shouldThrowPaymentNotFoundException_whenTransactionMissing() {
        UUID randomId = UUID.randomUUID();
        when(transactionRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(randomId))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }

    // ── getPaymentByOrderId ───────────────────────────────────────────────────

    @Test
    void getPaymentByOrderId_shouldReturnResponse_whenExists() {
        when(transactionRepository.findByOrderId(1001L)).thenReturn(Optional.of(sampleTransaction));
        when(paymentMapper.toPaymentResponse(sampleTransaction)).thenReturn(sampleResponse);

        PaymentResponse result = paymentService.getPaymentByOrderId(1001L);

        assertThat(result).isEqualTo(sampleResponse);
        verify(transactionRepository).findByOrderId(1001L);
    }

    @Test
    void getPaymentByOrderId_shouldThrowPaymentNotFoundException_whenOrderHasNoPayment() {
        when(transactionRepository.findByOrderId(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByOrderId(9999L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("9999");
    }

    // ── getCustomerPayments ───────────────────────────────────────────────────

    @Test
    void getCustomerPayments_shouldReturnEmptyList_whenNoPayments() {
        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong())).thenReturn(java.util.List.of());

        var result = paymentService.getCustomerPayments(100L);

        assertThat(result).isEmpty();
    }
}
