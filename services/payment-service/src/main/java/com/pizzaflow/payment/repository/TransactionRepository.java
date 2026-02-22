package com.pizzaflow.payment.repository;

import com.pizzaflow.payment.model.Transaction;
import com.pizzaflow.payment.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByOrderId(Long orderId);

    List<Transaction> findByCustomerId(Long customerId);

    List<Transaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Transaction> findByStatus(PaymentStatus status);

    Optional<Transaction> findByGatewayTransactionId(String gatewayTransactionId);

    boolean existsByOrderId(Long orderId);
}
