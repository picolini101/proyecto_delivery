package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderLineId;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.springframework.stereotype.Component;
import java.util.NoSuchElementException;
import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;

@Component
@ConditionalOnJpaPersistence
public class OrderLinePersistenceMapper
        implements PersistenceMapper<OrderLine, OrderLineId, OrderLineJpaEntity, UUID> {

    private final OrderJpaRepository orderRepository;

    public OrderLinePersistenceMapper(OrderJpaRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderLineJpaEntity toEntity(OrderLine line) {
        if (line.getOrderId() == null) {
            throw new IllegalArgumentException("OrderLine debe estar asociada a un pedido");
        }
        OrderJpaEntity order = orderRepository.findById(line.getOrderId().value())
                .orElseThrow(() -> new NoSuchElementException("No existe el pedido " + line.getOrderId()));
        return toEntity(line, order);
    }

    OrderLineJpaEntity toEntity(OrderLine line, OrderJpaEntity order) {
        return new OrderLineJpaEntity(line.getId().value(), line.getProductId().value(), line.getProductName(),
                line.getUnitPrice().value().doubleValue(), line.getQuantity().value(), line.getCommerceId(),
                line.getSubtotal().value().doubleValue(), order, line.getCreatedAt(), line.getUpdatedAt());
    }

    List<OrderLineJpaEntity> toEntities(List<OrderLine> lines, OrderJpaEntity order) {
        return lines.stream().map(line -> toEntity(line, order)).toList();
    }

    @Override
    public OrderLine toDomain(OrderLineJpaEntity entity) {
        return OrderLine.restore(OrderLineId.of(entity.getId()), ProductId.of(entity.getProductId()),
                entity.getCommerceId(), entity.getProductName(), Money.of(entity.getUnitPrice()),
                Quantity.of(entity.getQuantity()), Money.of(entity.getSubtotal()),
                OrderId.of(entity.getOrder().getId()), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    List<OrderLine> toDomain(List<OrderLineJpaEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public UUID toEntityId(OrderLineId id) {
        return id.value();
    }
}
