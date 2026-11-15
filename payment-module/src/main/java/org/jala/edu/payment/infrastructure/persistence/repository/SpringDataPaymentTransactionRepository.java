package org.jala.edu.payment.infrastructure.persistence.repository;

import org.jala.edu.payment.infrastructure.persistence.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para consultar la tabla pay_payment_transactions.
 */
@Repository
public interface SpringDataPaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, String> {
    List<PaymentTransactionEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    List<PaymentTransactionEntity> findByOrderIdOrderByCreatedAtDesc(String orderId);
    boolean existsByOrderIdAndStatus(String orderId, String status);
}