package org.jala.edu.payment.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador unico de una transaccion de pago.
 */

public record PaymentTransactionId(UUID value) {

    public PaymentTransactionId {
        Objects.requireNonNull(value, "Payment transaction id cannot be null");
    }
    public static PaymentTransactionId newId() {
        return new PaymentTransactionId(UUID.randomUUID());
    }
    public static PaymentTransactionId of(String value) {
        return new PaymentTransactionId(UUID.fromString(value));
    }
    @Override
    public String toString() {
        return value.toString();
    }
}
