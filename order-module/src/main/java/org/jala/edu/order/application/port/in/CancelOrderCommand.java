package org.jala.edu.order.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record CancelOrderCommand(UUID orderId, UUID customerId, String reason) {

    public CancelOrderCommand {
        Objects.requireNonNull(orderId, "El orderId es obligatorio");
        Objects.requireNonNull(customerId, "El customerId es obligatorio");
    }
}