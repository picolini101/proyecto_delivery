package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.jala.edu.core.infrastructure.persistence.jpa.BaseEntityRepository;

public interface OrderStatusHistoryJpaRepository
        extends BaseEntityRepository<OrderStatusHistoryJpaEntity, UUID> {

    List<OrderStatusHistoryJpaEntity> findByOrderIdOrderByCreatedAtDesc(UUID orderId);
}
