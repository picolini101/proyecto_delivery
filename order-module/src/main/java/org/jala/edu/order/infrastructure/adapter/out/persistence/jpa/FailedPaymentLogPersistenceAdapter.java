package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.jala.edu.core.infrastructure.persistence.jpa.JpaCrudRepositoryAdapter;
import org.jala.edu.order.application.port.out.FailedPaymentLogPort;
import org.jala.edu.order.domain.model.FailedPaymentRecord;
import org.jala.edu.order.domain.model.FailedPaymentRecordId;
import org.jala.edu.order.domain.model.OrderId;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnJpaPersistence
public class FailedPaymentLogPersistenceAdapter extends
        JpaCrudRepositoryAdapter<FailedPaymentRecord, FailedPaymentRecordId, FailedPaymentJpaEntity, UUID>
        implements FailedPaymentLogPort {

    private final FailedPaymentJpaRepository repository;
    private final FailedPaymentPersistenceMapper mapper;

    public FailedPaymentLogPersistenceAdapter(FailedPaymentJpaRepository repository,
                                              FailedPaymentPersistenceMapper mapper) {
        super(repository, mapper);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<FailedPaymentRecord> findByOrderId(OrderId orderId) {
        return repository.findByOrderIdOrderByCreatedAtAsc(orderId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }
}