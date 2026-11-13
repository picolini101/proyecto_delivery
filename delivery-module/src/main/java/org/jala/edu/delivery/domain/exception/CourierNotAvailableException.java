package org.jala.edu.delivery.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * The courier is marked as not available or is already busy with another order, so they cannot take a new one.
 */
public class CourierNotAvailableException extends DomainException {

    public CourierNotAvailableException() {
        super("El repartidor no está disponible para tomar pedidos");
    }
}
