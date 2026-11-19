package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.DeliveryAddress;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatus;
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
        // Rescatar el envío. Si viene nulo o es 0, le ponemos 10.00
        java.math.BigDecimal shippingVal = entity.getShippingCost();
        if (shippingVal == null || shippingVal.compareTo(java.math.BigDecimal.ZERO) == 0) {
            shippingVal = new java.math.BigDecimal("10.00");
        }

        //Rescatar el total. Si viene nulo o es 0, calculamos (Subtotal + Envío)
        java.math.BigDecimal totalVal = entity.getTotal();
        if (totalVal == null || totalVal.compareTo(java.math.BigDecimal.ZERO) == 0) {
            totalVal = entity.getSubtotal().add(shippingVal);
        }

        return Order.restore(OrderId.of(entity.getId()),
                CustomerId.of(entity.getCustomerId()),
                orderLinePersistenceMapper.toDomain(entity.getProducts()),
                Money.of(entity.getSubtotal()),
                OrderStatus.valueOf(entity.getStatus()),
                entity.getDeliveryCity() == null ? null : new DeliveryAddress(entity.getDeliveryCity(),
                        entity.getDeliveryAvenue(), entity.getDeliveryHouseNumber()),
                Money.of(shippingVal),
                Money.of(totalVal),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    @Override
    public java.util.UUID toEntityId(OrderId orderId) {
        return orderId.value();
    }

}
