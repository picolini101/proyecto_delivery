package org.jala.edu.order.application.port.out;

import org.jala.edu.core.application.port.out.CrudRepositoryPort;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort extends CrudRepositoryPort<Order, OrderId> {

    List<Order> findByCustomerId(CustomerId customerId);
}