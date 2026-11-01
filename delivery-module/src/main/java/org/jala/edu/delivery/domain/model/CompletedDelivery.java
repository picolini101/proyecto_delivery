package org.jala.edu.delivery.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * A delivery the courier has already finished: the order, the courier who delivered it, the amount earned,
 * the locality it was delivered to and the day it was delivered.
 */
public record CompletedDelivery(UUID orderId, UUID courierId, BigDecimal amount, String locality, LocalDate deliveredOn) {

    public CompletedDelivery {
        Objects.requireNonNull(orderId, "Order id is required");
        Objects.requireNonNull(courierId, "Courier id is required");
        Objects.requireNonNull(amount, "Amount is required");
        Objects.requireNonNull(deliveredOn, "Delivery date is required");
        if (locality == null || locality.isBlank()) {
            throw new DomainException("Locality cannot be blank");
        }
        if (amount.signum() < 0) {
            throw new DomainException("Amount cannot be negative");
        }
        locality = locality.strip();
    }
}
