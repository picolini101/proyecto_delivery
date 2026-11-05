package org.jala.edu.payment.infrastructure.persistence.adapter;

import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.jala.edu.payment.application.port.out.PaymentTransactionRepositoryPort;
import org.jala.edu.payment.domain.model.PaymentStatus;
import org.jala.edu.payment.domain.model.PaymentTransaction;
import org.jala.edu.payment.domain.model.PaymentTransactionId;
import org.jala.edu.payment.infrastructure.persistence.entity.PaymentTransactionEntity;
import org.jala.edu.payment.infrastructure.persistence.repository.SpringDataPaymentTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador JPA que guarda y consulta transacciones de pago en MySQL.
 */
@Component
@ConditionalOnJpaPersistence
public class PaymentTransactionRepositoryAdapter implements PaymentTransactionRepositoryPort {
    private final SpringDataPaymentTransactionRepository springDataRepository;
    public PaymentTransactionRepositoryAdapter(SpringDataPaymentTransactionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }
    @Override
    public PaymentTransaction save(PaymentTransaction transaction) {
        PaymentTransactionEntity entity = toEntity(transaction);
        PaymentTransactionEntity savedEntity = springDataRepository.save(entity);
        return toDomain(savedEntity);
    }
    @Override
    public List<PaymentTransaction> findByUserId(UUID userId) {
        return springDataRepository.findByUserIdOrderByCreatedAtDesc(userId.toString()).stream()
                .map(this::toDomain)
                .toList();
    }
    @Override
    public boolean existsApprovedByOrderId(UUID orderId) {
        return springDataRepository.existsByOrderIdAndStatus(
                orderId.toString(), PaymentStatus.APPROVED.name());
    }
    private PaymentTransactionEntity toEntity(PaymentTransaction domain) {
        return new PaymentTransactionEntity(
                domain.getId().value().toString(),
                domain.getOrderId().toString(),
                domain.getUserId().toString(),
                domain.getPaymentMethodId().toString(),
                domain.getAmount(),
                domain.getStatus().name(),
                domain.getReason(),
                domain.getCreatedAt()
        );
    }
    private PaymentTransaction toDomain(PaymentTransactionEntity entity) {
        return PaymentTransaction.restore(
                new PaymentTransactionId(UUID.fromString(entity.getId())),
                UUID.fromString(entity.getOrderId()),
                UUID.fromString(entity.getUserId()),
                UUID.fromString(entity.getPaymentMethodId()),
                entity.getAmount(),
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getReason(),
                entity.getCreatedAt(),
                entity.getCreatedAt()
        );
    }
}
