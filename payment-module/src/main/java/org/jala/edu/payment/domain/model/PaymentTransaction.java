package org.jala.edu.payment.domain.model;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.model.BaseModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa un intento de pago registrado en el historial financiero.
 * Es inmutable (RNF-006): una vez creada la transaccion, sus datos no cambian.
 */

public class PaymentTransaction extends BaseModel<PaymentTransactionId> {
    private final PaymentTransactionId transactionId;
    private final UUID orderId;
    private final UUID userId;
    private final BigDecimal amount;
    private final PaymentStatus status;
    private final String reason;
    private final Instant transactionCreatedAt;
    private PaymentTransaction(PaymentTransactionId id, UUID orderId, UUID userId, BigDecimal amount,
                               PaymentStatus status, String reason, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.transactionId = id;
        this.orderId = Objects.requireNonNull(orderId, "Order id is required");
        this.userId = Objects.requireNonNull(userId, "User id is required");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("El monto del pago debe ser mayor a cero");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.status = Objects.requireNonNull(status, "Payment status is required");
        this.reason = reason == null ? "" : reason.strip();
        this.transactionCreatedAt = createdAt;
    }
    /**
     * Crea una nueva transaccion en estado APROBADO.
     */
    public static PaymentTransaction recordApproved(UUID orderId, UUID userId, BigDecimal amount) {
        Instant now = now();
        return new PaymentTransaction(
                PaymentTransactionId.newId(), orderId, userId, amount, PaymentStatus.APPROVED, "", now, now);
    }
    /**
     * Crea una nueva transaccion en estado RECHAZADO indicando el motivo.
     */
    public static PaymentTransaction recordRejected(UUID orderId, UUID userId, BigDecimal amount, String reason) {
        Instant now = now();
        return new PaymentTransaction(
                PaymentTransactionId.newId(), orderId, userId, amount, PaymentStatus.REJECTED, reason, now, now);
    }
    /**
     * Reconstruye una transaccion guardada en la base de datos.
     */
    public static PaymentTransaction restore(PaymentTransactionId id, UUID orderId, UUID userId, BigDecimal amount,
                                             PaymentStatus status, String reason,
                                             Instant createdAt, Instant updatedAt) {
        return new PaymentTransaction(id, orderId, userId, amount, status, reason, createdAt, updatedAt);
    }
    public PaymentTransactionId getId() {
        return transactionId;
    }
    public Instant getCreatedAt() {
        return transactionCreatedAt;
    }
    public UUID getOrderId() {
        return orderId;
    }
    public UUID getUserId() {
        return userId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public PaymentStatus getStatus() {
        return status;
    }
    public String getReason() {
        return reason;
    }
    public boolean isApproved() {
        return status == PaymentStatus.APPROVED;
    }
}
