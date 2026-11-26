package org.jala.edu.order.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record MarkOrderReadyForDeliveryCommand(UUID orderId, UUID merchantId, UUID changedById) {

    public MarkOrderReadyForDeliveryCommand {
        Objects.requireNonNull(orderId, "Order id is required");
        Objects.requireNonNull(merchantId, "Merchant id is required");
        Objects.requireNonNull(changedById, "Changed by id is required");
    }
}
