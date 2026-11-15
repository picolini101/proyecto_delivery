package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import org.jala.edu.core.infrastructure.persistence.jpa.BaseEntityRepository;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;

import java.util.List;

public interface OrderJpaRepository extends BaseEntityRepository<OrderJpaEntity, OrderId> {
    List<Order> findByCustomerId(CustomerId customerId);
}
