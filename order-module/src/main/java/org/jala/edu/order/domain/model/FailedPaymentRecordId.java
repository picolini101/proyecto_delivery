package org.jala.edu.order.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identificador de un registro de intento de pago fallido. */
public record FailedPaymentRecordId(UUID value) {

    public FailedPaymentRecordId {
        Objects.requireNonNull(value, "FailedPaymentRecordId no puede ser nulo");
    }

    public static FailedPaymentRecordId newId() {
        return new FailedPaymentRecordId(UUID.randomUUID());
    }

    public static FailedPaymentRecordId of(UUID value) {
        return new FailedPaymentRecordId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}