package org.jala.edu.delivery.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * Another courier took the order first. It is a {@link DomainException} so the screens show its message as it is.
 */
public class ConcurrencyException extends DomainException {

    public static final String MESSAGE = "El pedido ya ha sido tomado por otro repartidor.";

    public ConcurrencyException(Throwable cause) {
        super(MESSAGE);
        initCause(cause);
    }
}
