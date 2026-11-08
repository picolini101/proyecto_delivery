package org.jala.edu.order.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

import java.math.BigDecimal;

public class InvalidMoneyValueException extends DomainException {

    private final BigDecimal invalidValue;

    public InvalidMoneyValueException(BigDecimal value) {
        super("El importe monetario no puede ser negativo: " + value);
        this.invalidValue = value;
    }

    public BigDecimal getInvalidValue() {
        return invalidValue;
    }
}