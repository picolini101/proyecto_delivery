package org.jala.edu.order.application.port.in;

import java.util.Objects;
import java.util.UUID;

import org.jala.edu.order.application.dto.PaymentAttempt;

/**
 * Datos de entrada del caso de uso {@code RegisterFailedPaymentUseCase}.
 * Un {@code record} es una clase corta que solo guarda datos; se leen con {@code command.attempt()}.
 *
 * @param attempt    el intento de pago que devolvio {@code AuthorizePaymentUseCase}
 * @param customerId id del cliente que intento pagar
 */
public record RegisterFailedPaymentCommand(PaymentAttempt attempt, UUID customerId) {

    /** Valida que ningun dato falte. */
    public RegisterFailedPaymentCommand {
        Objects.requireNonNull(attempt, "Attempt no puede ser nulo");
        Objects.requireNonNull(customerId, "CustomerId no puede ser nulo");
    }
}