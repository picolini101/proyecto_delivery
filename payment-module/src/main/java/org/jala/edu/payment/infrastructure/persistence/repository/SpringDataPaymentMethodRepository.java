package org.jala.edu.payment.infrastructure.persistence.repository;

import org.jala.edu.payment.infrastructure.persistence.entity.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpringDataPaymentMethodRepository extends JpaRepository<PaymentMethodEntity, UUID> {
    List<PaymentMethodEntity> findByUserId(UUID userId);
}