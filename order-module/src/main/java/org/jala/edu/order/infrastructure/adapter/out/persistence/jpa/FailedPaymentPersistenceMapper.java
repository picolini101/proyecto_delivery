package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.UUID;
import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.order.domain.model.FailedPaymentRecord;
import org.jala.edu.order.domain.model.FailedPaymentRecordId;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.PaymentAttemptResult;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnJpaPersistence
class FailedPaymentPersistenceMapper
        implements PersistenceMapper<FailedPaymentRecord, FailedPaymentRecordId, FailedPaymentJpaEntity, UUID> {

    @Override
    public FailedPaymentJpaEntity toEntity(FailedPaymentRecord record) {
        return new FailedPaymentJpaEntity(record.getId().value(), record.getOrderId().value(),
                record.getCustomerId(), record.getResult().name(), record.getReason(),
                record.getCreatedAt(), record.getUpdatedAt());
    }

    @Override
    public FailedPaymentRecord toDomain(FailedPaymentJpaEntity entity) {
        return FailedPaymentRecord.restore(FailedPaymentRecordId.of(entity.getId()),
                OrderId.of(entity.getOrderId()), entity.getCustomerId(),
                PaymentAttemptResult.valueOf(entity.getPaymentResult()), entity.getReason(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    @Override
    public UUID toEntityId(FailedPaymentRecordId id) {
        return id.value();
    }
}