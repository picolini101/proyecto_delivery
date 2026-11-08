package org.jala.edu.order.infrastructure.adapter.out.persistence.memory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.core.infrastructure.persistence.memory.InMemoryCrudRepository;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnInMemoryPersistence
public class InMemoryOrderRepositoryAdapter extends InMemoryCrudRepository<Order,OrderId > implements OrderRepositoryPort {

    private final Map<OrderId, Order> storage = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return storage.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .toList();
    }
}