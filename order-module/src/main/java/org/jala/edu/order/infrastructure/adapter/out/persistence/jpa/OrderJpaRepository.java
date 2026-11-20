package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import org.jala.edu.core.infrastructure.persistence.jpa.BaseEntityRepository;
import java.util.UUID;

import java.util.List;

public interface OrderJpaRepository extends BaseEntityRepository<OrderJpaEntity, UUID> {
    List<OrderJpaEntity> findByCustomerId(UUID customerId);
}
