package org.jala.edu.order.application.port.in;

import java.util.Objects;
import java.util.UUID;
import org.jala.edu.order.domain.model.OrderStatus;
public record ChangeOrderStatusCommand(
        UUID orderId,
        OrderStatus newStatus,
        UUID changedById,
        String reason
) {
    public ChangeOrderStatusCommand {
        Objects.requireNonNull(orderId, "OrderId no puede ser nulo");
        Objects.requireNonNull(newStatus, "newStatus no puede ser nulo");
        Objects.requireNonNull(changedById, "changedById no puede ser nulo");
    }
}
