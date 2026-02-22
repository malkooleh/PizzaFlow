package com.pizzaflow.payment.repository;

import com.pizzaflow.payment.model.PaymentMethod;
import com.pizzaflow.payment.model.enums.PaymentMethodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    List<PaymentMethod> findByCustomerIdAndIsActiveTrue(Long customerId);

    Optional<PaymentMethod> findByCustomerIdAndIsDefaultTrue(Long customerId);

    List<PaymentMethod> findByCustomerIdAndType(Long customerId, PaymentMethodType type);

    boolean existsByCustomerIdAndIsDefaultTrue(Long customerId);
}
