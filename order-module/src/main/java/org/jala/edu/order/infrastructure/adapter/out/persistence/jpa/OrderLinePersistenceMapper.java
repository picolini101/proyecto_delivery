package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.order.domain.model.*;

import java.util.ArrayList;
import java.util.List;

public class OrderLinePersistenceMapper implements PersistenceMapper<OrderLine, OrderLineId, OrderLineJpaEntity, String> {

    @Override
    public OrderLineJpaEntity toEntity(OrderLine orderLine) {
        return new OrderLineJpaEntity(orderLine.getProductId(),
                orderLine.getProductName(),
                orderLine.getUnitPrice(),
                orderLine.getQuantity(),
                orderLine.getSubtotal(),
                orderLine.getCommerceId());
    }

    @Override
    public OrderLine toDomain(OrderLineJpaEntity entity) {
        return OrderLine.restore(OrderLineId.of(entity.getOrder().getId()),
                ProductId.of(entity.getId()),
                entity.getMerchantId(),
                entity.getProductName(),
                Money.of(entity.getUnitPrice()),
                Quantity.of(entity.getQuantity()),
                Money.of(entity.getSubtotal()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    @Override
    public String toEntityId(OrderLineId orderLineId) {
        return orderLineId.toString();
    }
    public List<OrderLineJpaEntity> toListProductEntity(List<OrderLine> products){
        List<OrderLineJpaEntity> newList=new ArrayList<>();
        for(OrderLine orderLine : products){
            newList.add(toEntity(orderLine));
        }
        return newList;
    }
    public List<OrderLine> toOrderLine(List<OrderLineJpaEntity> products){
        List<OrderLine> newList=new ArrayList<>();
        for(OrderLineJpaEntity orderLineJpa : products){
            newList.add(toDomain(orderLineJpa));
        }
        return newList;
    }
}
