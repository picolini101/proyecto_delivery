package org.jala.edu.payment.domain.model;

import java.util.UUID;

/**
 * Identificador unico de una transaccion de pago.
 */

public record PaymentTransactionId(UUID value) {
    public static PaymentTransactionId newId() {
        return new PaymentTransactionId(UUID.randomUUID());
    }
}