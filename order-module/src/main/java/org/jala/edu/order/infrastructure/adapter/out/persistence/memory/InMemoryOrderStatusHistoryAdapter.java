package org.jala.edu.order.infrastructure.adapter.out.persistence.memory;

import java.util.ArrayList;
import java.util.List;
import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.core.infrastructure.persistence.memory.InMemoryCrudRepository;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatusChange;
import org.jala.edu.order.domain.model.OrderStatusChangeId;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnInMemoryPersistence
public class InMemoryOrderStatusHistoryAdapter extends InMemoryCrudRepository<OrderStatusChange, OrderStatusChangeId>
        implements OrderStatusHistoryPort {

    @Override
    public List<OrderStatusChange> findByOrderId(OrderId orderId) {
        List<OrderStatusChange> result = new ArrayList<OrderStatusChange>();
        for (OrderStatusChange change : findAll()) {
            if (change.getOrderId().equals(orderId)) {
                result.add(change);
            }
        }
        return result;
    }
}