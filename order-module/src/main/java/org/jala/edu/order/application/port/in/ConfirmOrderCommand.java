package org.jala.edu.order.application.port.in;

import java.util.Objects;
import java.util.UUID;
import org.jala.edu.order.application.dto.PaymentAttempt;

public record ConfirmOrderCommand(UUID orderId, UUID customerId, PaymentAttempt paymentAttempt) {

    public ConfirmOrderCommand {
        Objects.requireNonNull(orderId, "El ID del pedido es obligatorio");
        Objects.requireNonNull(customerId, "El ID del cliente es obligatorio");
        Objects.requireNonNull(paymentAttempt, "El resultado del intento de pago es obligatorio");
    }
}