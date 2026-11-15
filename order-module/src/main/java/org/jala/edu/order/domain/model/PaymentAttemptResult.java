package org.jala.edu.order.domain.model;

/**
 * Resultado de un intento de pago que se guarda para auditoria.
 * Por ahora solo se registran los pagos rechazados.
 */
public enum PaymentAttemptResult {
    REJECTED
}