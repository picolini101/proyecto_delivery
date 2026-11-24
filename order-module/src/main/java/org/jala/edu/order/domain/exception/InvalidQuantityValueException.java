package org.jala.edu.order.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;


public class InvalidQuantityValueException extends DomainException {

    private final int invalidValue;

    public InvalidQuantityValueException(int value) {
        super("La cantidad no puede ser negativa: " + value);
        this.invalidValue = value;
    }

    public int getInvalidValue() {
        return invalidValue;
    }
}