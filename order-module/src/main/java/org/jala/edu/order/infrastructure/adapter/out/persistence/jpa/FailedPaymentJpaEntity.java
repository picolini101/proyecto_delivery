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
@Table(name = "failed_payment_log")
public class FailedPaymentJpaEntity extends BaseJpaEntity {

    @Id
    @Column(name = "failed_payment_id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "payment_result", nullable = false, length = 32)
    private String paymentResult;

    @Column(name = "reason", length = 500)
    private String reason;

    protected FailedPaymentJpaEntity() {
    }

    public FailedPaymentJpaEntity(UUID id, UUID orderId, UUID customerId, String paymentResult,
                                  String reason, Instant createdAt, Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentResult = paymentResult;
        this.reason = reason;
    }
}