package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatusChange;
import org.jala.edu.order.domain.model.OrderStatusChangeId;

public class OrderStatusHistoryPersistenceAdapter implements OrderStatusHistoryPort {

    @Override
    public OrderStatusChange save(OrderStatusChange model) {
        throw new UnsupportedOperationException("Order status history persistence is not implemented yet");
    }

    @Override
    public Optional<OrderStatusChange> findById(OrderStatusChangeId id) {
        throw new UnsupportedOperationException("Order status history persistence is not implemented yet");
    }

    @Override
    public List<OrderStatusChange> findAll() {
        throw new UnsupportedOperationException("Order status history persistence is not implemented yet");
    }

    @Override
    public boolean existsById(OrderStatusChangeId id) {
        throw new UnsupportedOperationException("Order status history persistence is not implemented yet");
    }

    @Override
    public void deleteById(OrderStatusChangeId id) {
        throw new UnsupportedOperationException("Order status history persistence is not implemented yet");
    }

    @Override
    public List<OrderStatusChange> findByOrderId(OrderId orderId) {
        throw new UnsupportedOperationException("Order status history persistence is not implemented yet");
    }
}
