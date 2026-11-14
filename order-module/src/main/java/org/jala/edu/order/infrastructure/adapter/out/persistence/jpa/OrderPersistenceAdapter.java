package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;

public class OrderPersistenceAdapter implements OrderRepositoryPort {

    @Override
    public Order save(Order model) {
        throw new UnsupportedOperationException("Order persistence is not implemented yet");
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        throw new UnsupportedOperationException("Order persistence is not implemented yet");
    }

    @Override
    public List<Order> findAll() {
        throw new UnsupportedOperationException("Order persistence is not implemented yet");
    }

    @Override
    public boolean existsById(OrderId id) {
        throw new UnsupportedOperationException("Order persistence is not implemented yet");
    }

    @Override
    public void deleteById(OrderId id) {
        throw new UnsupportedOperationException("Order persistence is not implemented yet");
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        throw new UnsupportedOperationException("Order persistence is not implemented yet");
    }
}
