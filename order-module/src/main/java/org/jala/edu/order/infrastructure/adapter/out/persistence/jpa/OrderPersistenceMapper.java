package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.order.domain.model.*;
import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnJpaPersistence
final class OrderPersistenceMapper implements PersistenceMapper<Order, OrderId, OrderJpaEntity, java.util.UUID> {
    final OrderLinePersistenceMapper orderLinePersistenceMapper;

    OrderPersistenceMapper(OrderLinePersistenceMapper orderLinePersistenceMapper) {
        this.orderLinePersistenceMapper = orderLinePersistenceMapper;
    }

    @Override
    public OrderJpaEntity toEntity(Order model) {
        OrderJpaEntity entity = new OrderJpaEntity(model.getId().value(),
                model.getCustomerId().value(),
                model.getSubtotal().value(),
                model.getStatus().toString(),
                model.getDeliveryAddress() == null ? null : model.getDeliveryAddress().city(),
                model.getDeliveryAddress() == null ? null : model.getDeliveryAddress().avenue(),
                model.getDeliveryAddress() == null ? null : model.getDeliveryAddress().houseNumber(),
                model.getShippingCost() == null ? null : model.getShippingCost().value(),
                model.getTotal() == null ? null : model.getTotal().value(),
                java.util.List.of(),
                model.getCreatedAt(),
                model.getUpdatedAt());
        entity.setProducts(orderLinePersistenceMapper.toEntities(model.getLines(), entity));
        return entity;
    }

    @Override
    public Order toDomain(OrderJpaEntity entity) {
        return Order.restore(OrderId.of(entity.getId()),
                CustomerId.of(entity.getCustomerId()),
                orderLinePersistenceMapper.toDomain(entity.getProducts()),
                Money.of(entity.getSubtotal()),
                OrderStatus.valueOf(entity.getStatus()),
                entity.getDeliveryCity() == null ? null : new DeliveryAddress(entity.getDeliveryCity(),
                        entity.getDeliveryAvenue(), entity.getDeliveryHouseNumber()),
                entity.getShippingCost() == null ? null : Money.of(entity.getShippingCost()),
                entity.getTotal() == null ? null : Money.of(entity.getTotal()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    @Override
    public java.util.UUID toEntityId(OrderId orderId) {
        return orderId.value();
    }

}
