
package org.jala.edu.payment.domain.model;

import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.model.BaseModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representa un intento de pago registrado en el historial financiero.
 */
public class PaymentTransaction extends BaseModel<PaymentTransactionId> {

    private final PaymentTransactionId transactionId;
    private final UUID orderId;
    private final UUID userId;
    private final UUID paymentMethodId;
    private final BigDecimal amount;
    private final PaymentStatus status;
    private final String reason;
    private final Instant transactionCreatedAt;

    private PaymentTransaction(
            PaymentTransactionId id,
            UUID orderId,
            UUID userId,
            UUID paymentMethodId,
            BigDecimal amount,
            PaymentStatus status,
            String reason,
            Instant createdAt,
            Instant updatedAt) {

        super(id, createdAt, updatedAt);

        if (orderId == null || userId == null
                || paymentMethodId == null || status == null) {
            throw new DomainException(
                    "Los datos de la transaccion son obligatorios");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException(
                    "El monto del pago debe ser mayor a cero");
        }

        this.transactionId = id;
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethodId = paymentMethodId;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.transactionCreatedAt = createdAt;
    }

    /**
     * Crea una nueva transaccion en estado APROBADO.
     */
    public static PaymentTransaction recordApproved(
            UUID orderId,
            UUID userId,
            UUID paymentMethodId,
            BigDecimal amount) {

        Instant now = now();

        return new PaymentTransaction(
                PaymentTransactionId.newId(),
                orderId,
                userId,
                paymentMethodId,
                amount,
                PaymentStatus.APPROVED,
                "",
                now,
                now);
    }

    /**
     * Crea una nueva transaccion en estado RECHAZADO indicando el motivo.
     */
    public static PaymentTransaction recordRejected(
            UUID orderId,
            UUID userId,
            UUID paymentMethodId,
            BigDecimal amount,
            String reason) {

        Instant now = now();

        return new PaymentTransaction(
                PaymentTransactionId.newId(),
                orderId,
                userId,
                paymentMethodId,
                amount,
                PaymentStatus.REJECTED,
                reason,
                now,
                now);
    }

    /**
     * Reconstruye una transaccion guardada en la base de datos.
     */
    // Se conservan todos los campos y las dos fechas de persistencia.
    @SuppressWarnings("checkstyle:ParameterNumber")
    public static PaymentTransaction restore(
            PaymentTransactionId id,
            UUID orderId,
            UUID userId,
            UUID paymentMethodId,
            BigDecimal amount,
            PaymentStatus status,
            String reason,
            Instant createdAt,
            Instant updatedAt) {

        return new PaymentTransaction(
                id,
                orderId,
                userId,
                paymentMethodId,
                amount,
                status,
                reason,
                createdAt,
                updatedAt);
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

    public UUID getPaymentMethodId() {
        return paymentMethodId;
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
