package com.pizzaflow.payment.service;

import com.pizzaflow.payment.dto.PaymentMethodDTO;
import com.pizzaflow.payment.dto.PaymentRequest;
import com.pizzaflow.payment.dto.PaymentResponse;
import com.pizzaflow.payment.dto.RefundRequest;
import com.pizzaflow.payment.dto.RefundResponse;
import com.pizzaflow.payment.mapper.PaymentMapper;
import com.pizzaflow.payment.model.PaymentMethod;
import com.pizzaflow.payment.model.Refund;
import com.pizzaflow.payment.model.Transaction;
import com.pizzaflow.payment.model.enums.PaymentMethodType;
import com.pizzaflow.payment.model.enums.PaymentStatus;
import com.pizzaflow.payment.model.enums.RefundStatus;
import com.pizzaflow.payment.repository.PaymentMethodRepository;
import com.pizzaflow.payment.repository.RefundRepository;
import com.pizzaflow.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final RefundRepository refundRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final KafkaProducerService kafkaProducerService;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        // Check if payment already exists for this order
        if (transactionRepository.existsByOrderId(request.getOrderId())) {
            throw new IllegalStateException("Payment already exists for order: " + request.getOrderId());
        }

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .orderId(request.getOrderId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PROCESSING)
                .paymentMethodType(request.getPaymentMethodType())
                .build();

        transaction = transactionRepository.save(transaction);

        // Get card last four for gateway simulation
        String cardLastFour = extractCardLastFour(request);

        // Process payment through gateway
        PaymentGatewayService.GatewayResponse gatewayResponse = paymentGatewayService.processPayment(
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentMethodType(),
                cardLastFour);

        // Update transaction with gateway response
        transaction.setPaymentGateway(gatewayResponse.getGatewayName());
        transaction.setGatewayTransactionId(gatewayResponse.getGatewayTransactionId());
        transaction.setGatewayResponse(gatewayResponse.getMessage());

        if (gatewayResponse.isSuccess()) {
            transaction.setStatus(PaymentStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());

            // Save payment method if requested
            if (Boolean.TRUE.equals(request.getSavePaymentMethod()) && request.getCardNumber() != null) {
                savePaymentMethod(request, cardLastFour);
            }

            transaction = transactionRepository.save(transaction);

            // Publish success event
            kafkaProducerService.publishPaymentCompleted(
                    transaction.getId(),
                    transaction.getOrderId(),
                    transaction.getCustomerId(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    transaction.getPaymentMethodType().name(),
                    transaction.getGatewayTransactionId());

            log.info("Payment completed successfully for order: {}", request.getOrderId());
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setFailureReason(gatewayResponse.getMessage());

            transaction = transactionRepository.save(transaction);

            // Publish failure event
            kafkaProducerService.publishPaymentFailed(
                    transaction.getId(),
                    transaction.getOrderId(),
                    transaction.getCustomerId(),
                    transaction.getAmount(),
                    gatewayResponse.getMessage(),
                    gatewayResponse.getErrorCode());

            log.warn("Payment failed for order: {}, reason: {}",
                    request.getOrderId(), gatewayResponse.getMessage());
        }

        return paymentMapper.toPaymentResponse(transaction);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        return paymentMapper.toPaymentResponse(transaction);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Transaction transaction = transactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Transaction not found for order: " + orderId));
        return paymentMapper.toPaymentResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getCustomerPayments(Long customerId) {
        return transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(paymentMapper::toPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RefundResponse processRefund(RefundRequest request) {
        log.info("Processing refund for transaction: {}", request.getTransactionId());

        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + request.getTransactionId()));

        // Validate refund amount
        BigDecimal totalRefunded = transaction.getRefunds().stream()
                .filter(r -> r.getStatus() == RefundStatus.COMPLETED)
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal availableForRefund = transaction.getAmount().subtract(totalRefunded);

        if (request.getAmount().compareTo(availableForRefund) > 0) {
            throw new IllegalArgumentException(
                    "Refund amount exceeds available amount. Available: " + availableForRefund);
        }

        // Create refund record
        Refund refund = Refund.builder()
                .transaction(transaction)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .reason(request.getReason())
                .status(RefundStatus.PROCESSING)
                .build();

        // Process refund through gateway
        PaymentGatewayService.GatewayResponse gatewayResponse = paymentGatewayService.processRefund(
                transaction.getGatewayTransactionId(),
                request.getAmount());

        if (gatewayResponse.isSuccess()) {
            refund.setStatus(RefundStatus.COMPLETED);
            refund.setCompletedAt(LocalDateTime.now());
            refund.setGatewayRefundId(gatewayResponse.getGatewayTransactionId());

            // Update transaction status
            BigDecimal newTotalRefunded = totalRefunded.add(request.getAmount());
            if (newTotalRefunded.compareTo(transaction.getAmount()) >= 0) {
                transaction.setStatus(PaymentStatus.REFUNDED);
            } else {
                transaction.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }

            transaction.addRefund(refund);
            transactionRepository.save(transaction);

            // Publish refund event
            kafkaProducerService.publishRefundCompleted(
                    refund.getId(),
                    transaction.getId(),
                    request.getOrderId(),
                    transaction.getCustomerId(),
                    request.getAmount(),
                    request.getReason());

            log.info("Refund completed for transaction: {}", request.getTransactionId());
        } else {
            refund.setStatus(RefundStatus.FAILED);
            transaction.addRefund(refund);
            transactionRepository.save(transaction);

            log.warn("Refund failed for transaction: {}", request.getTransactionId());
        }

        return paymentMapper.toRefundResponse(refund);
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodDTO> getCustomerPaymentMethods(Long customerId) {
        return paymentMethodRepository.findByCustomerIdAndIsActiveTrue(customerId)
                .stream()
                .map(paymentMapper::toPaymentMethodDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePaymentMethod(UUID paymentMethodId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found: " + paymentMethodId));

        paymentMethod.setIsActive(false);
        paymentMethodRepository.save(paymentMethod);

        log.info("Payment method deactivated: {}", paymentMethodId);
    }

    private String extractCardLastFour(PaymentRequest request) {
        if (request.getCardNumber() != null && request.getCardNumber().length() >= 4) {
            return request.getCardNumber().substring(request.getCardNumber().length() - 4);
        }
        return null;
    }

    private void savePaymentMethod(PaymentRequest request, String cardLastFour) {
        // Check if this is the first payment method for the customer
        boolean isFirst = !paymentMethodRepository.existsByCustomerIdAndIsDefaultTrue(request.getCustomerId());

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .customerId(request.getCustomerId())
                .type(request.getPaymentMethodType())
                .cardLastFour(cardLastFour)
                .cardBrand(detectCardBrand(request.getCardNumber()))
                .cardExpiryMonth(request.getExpiryMonth())
                .cardExpiryYear(request.getExpiryYear())
                .isDefault(isFirst)
                .isActive(true)
                .build();

        paymentMethodRepository.save(paymentMethod);
        log.info("Payment method saved for customer: {}", request.getCustomerId());
    }

    private String detectCardBrand(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return null;
        }
        String number = cardNumber.replaceAll("\\s", "");
        if (number.startsWith("4"))
            return "VISA";
        if (number.startsWith("5"))
            return "MASTERCARD";
        if (number.startsWith("34") || number.startsWith("37"))
            return "AMEX";
        if (number.startsWith("6011") || number.startsWith("65"))
            return "DISCOVER";
        return "UNKNOWN";
    }

    /**
     * Process automatic payment when order is created (for non-COD orders).
     * Called from Kafka listener.
     */
    @Transactional
    public void processOrderPayment(Long orderId, Long customerId, BigDecimal amount) {
        log.info("Auto-processing payment for order: {}", orderId);

        // Try to use customer's default payment method
        PaymentMethod defaultMethod = paymentMethodRepository.findByCustomerIdAndIsDefaultTrue(customerId)
                .orElse(null);

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .currency("USD")
                .paymentMethodType(defaultMethod != null ? defaultMethod.getType() : PaymentMethodType.CREDIT_CARD)
                .cardNumber(defaultMethod != null ? "****" + defaultMethod.getCardLastFour() : "4111111111111111")
                .build();

        processPayment(paymentRequest);
    }
}
