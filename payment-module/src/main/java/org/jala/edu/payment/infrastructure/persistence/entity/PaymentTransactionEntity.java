package org.jala.edu.payment.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad JPA para guardar transacciones de pago en la tabla pay_payment_transactions.
 */
@Entity
@Table(name = "pay_payment_transactions")
public class PaymentTransactionEntity {
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;
    @Column(name = "order_id", length = 36, nullable = false)
    private String orderId;
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    @Column(name = "payment_method_id", length = 36, nullable = false)
    private String paymentMethodId;
    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    @Column(name = "reason", length = 255, nullable = false)
    private String reason;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    public PaymentTransactionEntity() {
    }
    public PaymentTransactionEntity(String id, String orderId, String userId, String paymentMethodId,
                                    BigDecimal amount, String status, String reason, Instant createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethodId = paymentMethodId;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }
    public String getId() {
        return id;
    }
    public String getOrderId() {
        return orderId;
    }
    public String getUserId() {
        return userId;
    }
    public String getPaymentMethodId() {
        return paymentMethodId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public String getStatus() {
        return status;
    }
    public String getReason() {
        return reason;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}

