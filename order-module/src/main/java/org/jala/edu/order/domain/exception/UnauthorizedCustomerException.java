package org.jala.edu.order.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

public class UnauthorizedCustomerException extends DomainException {
    public UnauthorizedCustomerException() {
        super("El usuario no es un cliente activo autorizado para iniciar compras.");
    }
    public UnauthorizedCustomerException(String message) {
        super(message);
    }
}
