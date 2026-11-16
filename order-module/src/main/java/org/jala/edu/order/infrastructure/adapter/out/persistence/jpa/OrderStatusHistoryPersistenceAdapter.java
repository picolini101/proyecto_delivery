package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.jala.edu.core.infrastructure.persistence.jpa.JpaCrudRepositoryAdapter;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatusChange;
import org.jala.edu.order.domain.model.OrderStatusChangeId;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnJpaPersistence
public class OrderStatusHistoryPersistenceAdapter extends
        JpaCrudRepositoryAdapter<OrderStatusChange, OrderStatusChangeId, OrderStatusHistoryJpaEntity, UUID>
        implements OrderStatusHistoryPort {

    private final OrderStatusHistoryJpaRepository repository;
    private final OrderStatusHistoryPersistenceMapper mapper;

    public OrderStatusHistoryPersistenceAdapter(OrderStatusHistoryJpaRepository repository,
                                                OrderStatusHistoryPersistenceMapper mapper) {
        super(repository, mapper);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<OrderStatusChange> findByOrderId(OrderId orderId) {
        return repository.findByOrderIdOrderByCreatedAtDesc(orderId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
