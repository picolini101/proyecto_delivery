package org.jala.edu.order.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException() {
        super("No se encontró el pedido indicado");
    }
}
