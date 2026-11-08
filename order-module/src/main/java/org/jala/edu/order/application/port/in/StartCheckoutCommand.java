package org.jala.edu.order.application.port.in;

import org.jala.edu.order.domain.model.CustomerId;

import java.util.Objects;
import java.util.UUID;

public record StartCheckoutCommand(UUID customerId) {
    public StartCheckoutCommand {
        Objects.requireNonNull(customerId, "CustomerId no puede ser nulo");
    }
}
