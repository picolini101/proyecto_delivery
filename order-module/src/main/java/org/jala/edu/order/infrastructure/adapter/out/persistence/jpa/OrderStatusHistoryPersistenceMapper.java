package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.UUID;
import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.OrderStatusChange;
import org.jala.edu.order.domain.model.OrderStatusChangeId;
import org.springframework.stereotype.Component;

@Component
class OrderStatusHistoryPersistenceMapper
        implements PersistenceMapper<OrderStatusChange, OrderStatusChangeId, OrderStatusHistoryJpaEntity, UUID> {

    @Override
    public OrderStatusHistoryJpaEntity toEntity(OrderStatusChange change) {
        return new OrderStatusHistoryJpaEntity(change.getId().value(), change.getOrderId().value(),
                change.getPreviousStatus().name(), change.getNewStatus().name(), change.getChangedById(),
                change.getReason(), change.getCreatedAt(), change.getUpdatedAt());
    }

    @Override
    public OrderStatusChange toDomain(OrderStatusHistoryJpaEntity entity) {
        return OrderStatusChange.restore(OrderStatusChangeId.of(entity.getId()), OrderId.of(entity.getOrderId()),
                OrderStatus.valueOf(entity.getPreviousStatus()), OrderStatus.valueOf(entity.getNewStatus()),
                entity.getChangedById(), entity.getReason(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    @Override
    public UUID toEntityId(OrderStatusChangeId id) {
        return id.value();
    }
}
