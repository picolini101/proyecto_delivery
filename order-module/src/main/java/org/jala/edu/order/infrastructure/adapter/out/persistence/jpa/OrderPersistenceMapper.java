package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.order.domain.model.*;

import java.util.ArrayList;
import java.util.List;

final class OrderPersistenceMapper implements PersistenceMapper<Order, OrderId, OrderJpaEntity, String> {
    final ProductPersistenceMapper productPersistenceMapper;

    OrderPersistenceMapper(ProductPersistenceMapper productPersistenceMapper) {
        this.productPersistenceMapper = productPersistenceMapper;
    }

    @Override
    public OrderJpaEntity toEntity(Order model) {
        return new OrderJpaEntity(toEntityId(model.getId()),
                model.getCustomerId(),
                model.getStatus(),
                model.getDeliveryAddress(),
                model.getShippingCost(),
                model.getTotal(),
                productPersistenceMapper.toListProductEntity(model.getLines()));
    }

    @Override
    public Order toDomain(OrderJpaEntity entity) {
        return Order.restore(OrderId.of(entity.getId()),
                CustomerId.of(entity.getCustomerId()),
                productPersistenceMapper.toOrderLine(entity.getProducts()),
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
