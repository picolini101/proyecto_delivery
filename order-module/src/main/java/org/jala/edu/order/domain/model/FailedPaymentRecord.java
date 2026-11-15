package org.jala.edu.order.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import org.jala.edu.core.domain.model.BaseModel;

/**
 * Registro de auditoria de un intento de pago fallido.
 * Guarda el pedido, el cliente, el resultado y el motivo. La fecha y la hora del intento
 * son {@code createdAt}, que {@link BaseModel} llena sola al crear el registro.
 */
@Getter
public class FailedPaymentRecord extends BaseModel<FailedPaymentRecordId> {

    private final OrderId orderId;
    private final UUID customerId;
    private final PaymentAttemptResult result;
    private final String reason;

    private FailedPaymentRecord(FailedPaymentRecordId id, OrderId orderId, UUID customerId,
                                PaymentAttemptResult result, String reason, Instant createdAt) {
        super(id, createdAt, createdAt);
        this.orderId = Objects.requireNonNull(orderId, "OrderId es obligatorio");
        this.customerId = Objects.requireNonNull(customerId, "CustomerId es obligatorio");
        this.result = Objects.requireNonNull(result, "El resultado es obligatorio");
        this.reason = reason;
    }

    /** Crea un registro nuevo con la fecha y hora actuales. */
    public static FailedPaymentRecord record(OrderId orderId, UUID customerId,
                                             PaymentAttemptResult result, String reason) {
        return new FailedPaymentRecord(FailedPaymentRecordId.newId(), orderId, customerId, result, reason, now());
    }
}