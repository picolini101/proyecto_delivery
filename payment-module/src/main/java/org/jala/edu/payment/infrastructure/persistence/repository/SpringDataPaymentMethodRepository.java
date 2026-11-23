package org.jala.edu.payment.infrastructure.persistence.repository;

import org.jala.edu.payment.infrastructure.persistence.entity.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataPaymentMethodRepository extends JpaRepository<PaymentMethodEntity, String> {
    List<PaymentMethodEntity> findByUserIdOrderByCreatedAtAsc(String userId);
}