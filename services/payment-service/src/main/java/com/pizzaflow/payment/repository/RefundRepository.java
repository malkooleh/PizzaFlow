package com.pizzaflow.payment.repository;

import com.pizzaflow.payment.model.Refund;
import com.pizzaflow.payment.model.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    List<Refund> findByTransactionId(UUID transactionId);

    List<Refund> findByOrderId(Long orderId);

    List<Refund> findByStatus(RefundStatus status);
}
