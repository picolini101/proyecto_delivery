package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.jala.edu.core.infrastructure.persistence.jpa.BaseJpaEntity;

@Getter
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistoryJpaEntity extends BaseJpaEntity {

    @Id
    @Column(name = "order_status_change_id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "previous_status", nullable = false, length = 32)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 32)
    private String newStatus;

    @Column(name = "changed_by_id", nullable = false)
    private UUID changedById;

    @Column(name = "reason", length = 500)
    private String reason;

    protected OrderStatusHistoryJpaEntity() {
    }

    public OrderStatusHistoryJpaEntity(UUID id, UUID orderId, String previousStatus, String newStatus,
                                       UUID changedById, String reason, Instant createdAt, Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedById = changedById;
        this.reason = reason;
    }
}
