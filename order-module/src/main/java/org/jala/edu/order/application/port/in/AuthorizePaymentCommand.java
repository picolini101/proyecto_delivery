package org.jala.edu.order.application.port.in;

import java.util.Objects;
import java.util.UUID;

/**
 * Datos de entrada del caso de uso {@code AuthorizePaymentUseCase} (HU-10).
 * Un {@code record} es una clase corta que solo guarda datos; se leen con {@code command.orderId()}.
 *
 * @param orderId         id del pedido que se quiere pagar (el que devolvio la finalizacion de compra)
 * @param customerId      id del cliente que paga
 * @param paymentMethodId id del metodo de pago; mientras no exista la pantalla para elegirlo, se usa
 *                        {@code SamplePaymentMethods.CUSTOMER_CARD}
 */
public record AuthorizePaymentCommand(UUID orderId, UUID customerId, UUID paymentMethodId) {

    /** Valida que ningun dato falte. */
    public AuthorizePaymentCommand {
        Objects.requireNonNull(orderId, "OrderId no puede ser nulo");
        Objects.requireNonNull(customerId, "CustomerId no puede ser nulo");
        Objects.requireNonNull(paymentMethodId, "PaymentMethodId no puede ser nulo");
    }
}
