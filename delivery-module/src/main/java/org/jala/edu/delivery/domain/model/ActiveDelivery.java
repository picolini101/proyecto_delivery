package org.jala.edu.delivery.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * A shipment a courier has started and has not closed yet.
 */
public record ActiveDelivery(UUID orderId, UUID courierId, DeliveryStep step, ConfirmationCode confirmationCode) {

    public ActiveDelivery {
        Objects.requireNonNull(orderId, "Order id is required");
        Objects.requireNonNull(courierId, "Courier id is required");
        Objects.requireNonNull(step, "Delivery step is required");
        Objects.requireNonNull(confirmationCode, "Confirmation code is required");
    }

    public boolean isInProgress() {
        return step == DeliveryStep.ON_THE_WAY || step == DeliveryStep.PICKED_UP;
    }

    public ActiveDelivery withStep(DeliveryStep next) {
        return new ActiveDelivery(orderId, courierId, next, confirmationCode);
    }
}
