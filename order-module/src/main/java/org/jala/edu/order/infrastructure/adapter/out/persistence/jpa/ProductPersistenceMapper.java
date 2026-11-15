package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.order.domain.model.*;

import java.util.ArrayList;
import java.util.List;

public class ProductPersistenceMapper implements PersistenceMapper<OrderLine, OrderLineId, ProductLineJpaEntity, String> {

    @Override
    public ProductLineJpaEntity toEntity(OrderLine orderLine) {
        return new ProductLineJpaEntity(orderLine.getProductId(),
                orderLine.getProductName(),
                orderLine.getUnitPrice(),
                orderLine.getQuantity(),
                orderLine.getSubtotal(),
                orderLine.getCommerceId());
    }

    @Override
    public OrderLine toDomain(ProductLineJpaEntity entity) {
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
    public List<ProductLineJpaEntity> toListProductEntity(List<OrderLine> products){
        List<ProductLineJpaEntity> newList=new ArrayList<>();
        for(OrderLine orderLine : products){
            newList.add(toEntity(orderLine));
        }
        return newList;
    }
    public List<OrderLine> toOrderLine(List<ProductLineJpaEntity> products){
        List<OrderLine> newList=new ArrayList<>();
        for(ProductLineJpaEntity orderLineJpa : products){
            newList.add(toDomain(orderLineJpa));
        }
        return newList;
    }
}
