package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.jala.edu.core.infrastructure.persistence.jpa.BaseEntityRepository;
import org.jala.edu.core.infrastructure.persistence.jpa.JpaCrudRepositoryAdapter;
import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnJpaPersistence
public class OrderPersistenceAdapter extends JpaCrudRepositoryAdapter<Order,OrderId,OrderJpaEntity,String> implements OrderRepositoryPort {
    private final OrderJpaRepository repository;
    protected OrderPersistenceAdapter(BaseEntityRepository<OrderJpaEntity, String> repository, PersistenceMapper<Order, OrderId, OrderJpaEntity, String> mapper, OrderJpaRepository repository1) {
        super(repository, mapper);
        this.repository = repository1;
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return repository.findByCustomerId(customerId);
    }
}
