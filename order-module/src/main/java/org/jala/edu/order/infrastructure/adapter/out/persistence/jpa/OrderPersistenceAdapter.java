package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.jala.edu.core.infrastructure.persistence.jpa.JpaCrudRepositoryAdapter;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@ConditionalOnJpaPersistence
public class OrderPersistenceAdapter extends JpaCrudRepositoryAdapter<Order, OrderId, OrderJpaEntity, UUID> implements OrderRepositoryPort {
    private final OrderJpaRepository repository;
    private final OrderPersistenceMapper mapper;
    public OrderPersistenceAdapter(OrderJpaRepository repository, OrderPersistenceMapper mapper) {
        super(repository, mapper);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return repository.findByCustomerId(customerId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
