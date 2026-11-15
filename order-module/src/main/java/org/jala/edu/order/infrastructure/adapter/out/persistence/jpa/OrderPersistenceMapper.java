package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.order.domain.model.*;

final class OrderPersistenceMapper implements PersistenceMapper<Order, OrderId, OrderJpaEntity, String> {
    final OrderLinePersistenceMapper orderLinePersistenceMapper;

    OrderPersistenceMapper(OrderLinePersistenceMapper orderLinePersistenceMapper) {
        this.orderLinePersistenceMapper = orderLinePersistenceMapper;
    }

    @Override
    public OrderJpaEntity toEntity(Order model) {
        return new OrderJpaEntity(model.getId().value(),
                model.getCustomerId().value(),
                model.getSubtotal().value(),
                model.getStatus().toString(),
                model.getDeliveryAddress().toString(),
                model.getShippingCost().value(),
                model.getTotal().value(),
                orderLinePersistenceMapper.toListProductEntity(model.getLines()),
                model.getCreatedAt(),
                model.getUpdatedAt());
    }

    @Override
    public Order toDomain(OrderJpaEntity entity) {
        return Order.restore(OrderId.of(entity.getId()),
                CustomerId.of(entity.getCustomerId()),
                orderLinePersistenceMapper.toOrderLine(entity.getProducts()),
                Money.of(entity.getSubtotal()),
                OrderStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    @Override
    public String toEntityId(OrderId orderId) {
        return orderId.toString();
    }

}
