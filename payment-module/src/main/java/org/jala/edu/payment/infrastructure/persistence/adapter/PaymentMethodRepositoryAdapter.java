package org.jala.edu.payment.infrastructure.persistence.adapter;

import org.jala.edu.payment.domain.model.Alias;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.model.PaymentType;
import org.jala.edu.payment.application.port.out.PaymentMethodRepositoryPort;
import org.jala.edu.payment.infrastructure.persistence.entity.PaymentMethodEntity;
import org.jala.edu.payment.infrastructure.persistence.repository.SpringDataPaymentMethodRepository;
import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@ConditionalOnJpaPersistence
public class PaymentMethodRepositoryAdapter implements PaymentMethodRepositoryPort {

    private final SpringDataPaymentMethodRepository springDataRepository;

    public PaymentMethodRepositoryAdapter(SpringDataPaymentMethodRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public PaymentMethod save(PaymentMethod domainModel) {
        PaymentMethodEntity entity = toEntity(domainModel);
        PaymentMethodEntity savedEntity = springDataRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<PaymentMethod> findById(UUID id) {
        return springDataRepository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public List<PaymentMethod> findByUserId(UUID userId) {
        return springDataRepository.findByUserIdOrderByCreatedAtAsc(userId.toString()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PaymentMethod> findLastRegisteredByUserId(UUID userId) {
        return springDataRepository.findByUserIdOrderByCreatedAtAsc(userId.toString()).stream()
                .filter(PaymentMethodEntity::isLastRegistered)
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public void clearLastRegisteredFlag(UUID userId) {
        List<PaymentMethodEntity> list = springDataRepository.findByUserIdOrderByCreatedAtAsc(userId.toString());
        for (PaymentMethodEntity entity : list) {
            if (entity.isLastRegistered()) {
                entity.setLastRegistered(false);
                springDataRepository.save(entity);
            }
        }
    }

    private PaymentMethodEntity toEntity(PaymentMethod domain) {
        return new PaymentMethodEntity(
                domain.getId().toString(),
                domain.getUserId().toString(),
                domain.getAlias().getValue(),
                domain.getType().name(),
                domain.getMaskedNumber(),
                domain.isLastRegistered(),
                domain.isActive());
    }

    private PaymentMethod toDomain(PaymentMethodEntity entity) {
        return new PaymentMethod(
                UUID.fromString(entity.getId()),
                UUID.fromString(entity.getUserId()),
                new Alias(entity.getAlias()),
                PaymentType.valueOf(entity.getType()),
                entity.getMaskedNumber(),
                entity.isLastRegistered(),
                entity.isActive());
    }
}