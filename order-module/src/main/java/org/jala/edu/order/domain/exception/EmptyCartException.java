package org.jala.edu.order.domain.exception;

import org.jala.edu.core.domain.exception.ConflictException;

public class EmptyCartException extends ConflictException {
    public EmptyCartException() {
        super("No se puede iniciar la finalización de compra con un carrito vacío.");
    }
    public EmptyCartException(String message) {
        super(message);
    }
}
