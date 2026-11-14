package org.jala.edu.order.application.port.out;

import java.util.List;
import org.jala.edu.core.application.port.out.CrudRepositoryPort;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatusChange;
import org.jala.edu.order.domain.model.OrderStatusChangeId;

public interface OrderStatusHistoryPort extends CrudRepositoryPort<OrderStatusChange, OrderStatusChangeId> {

    List<OrderStatusChange> findByOrderId(OrderId orderId);
}