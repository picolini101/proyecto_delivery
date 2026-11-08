package org.jala.edu.order.application.port.out;

import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findByCustomerId(CustomerId customerId);
}