package org.jala.edu.delivery.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.jala.edu.core.domain.exception.DomainException;

public record CompletedDelivery(UUID orderId, BigDecimal amount, String locality, LocalDate deliveredOn) {

    public CompletedDelivery {
        Objects.requireNonNull(orderId, "El id del pedido es obligatorio");
        Objects.requireNonNull(amount, "El monto es obligatorio");
        Objects.requireNonNull(locality, "La localidad es obligatoria");
        Objects.requireNonNull(deliveredOn, "La fecha de entrega es obligatoria");
        if (amount.signum() < 0) {
            throw new DomainException("El monto no puede ser negativo");
        }
    }
}
