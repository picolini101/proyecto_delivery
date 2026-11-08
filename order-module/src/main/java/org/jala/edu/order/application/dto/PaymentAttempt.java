package org.jala.edu.order.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.order.application.port.out.PaymentAuthorization;

/**
 * Resultado de {@code AuthorizePaymentUseCase}: lo que paso al pedir la autorizacion del pago.
 * <p>
 * Si {@link #isApproved()} es {@code true}, el siguiente paso (otra HU) es confirmar el pedido.
 * Si no, el pedido no continua; el motivo para mostrar al cliente esta en {@link #message()}.
 *
 * @param orderId       id del pedido que se intento pagar
 * @param amount        monto total que se pidio autorizar
 * @param authorization respuesta del servicio de pagos: aprobado, rechazado o no disponible
 */
public record PaymentAttempt(UUID orderId, BigDecimal amount, PaymentAuthorization authorization) {

    /** @return {@code true} si el servicio de pagos aprobo el monto */
    public boolean isApproved() {
        return authorization instanceof PaymentAuthorization.Approved;
    }

    /** @return mensaje para el cliente; vacio si el pago fue aprobado */
    public String message() {
        return authorization.message();
    }
}
