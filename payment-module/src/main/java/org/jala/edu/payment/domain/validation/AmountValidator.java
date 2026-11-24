package org.jala.edu.payment.domain.validation;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.jala.edu.core.domain.exception.DomainException;

public final class AmountValidator {

    private AmountValidator() {
    }

    public static BigDecimal validate(BigDecimal amount) {

        if (amount == null) {
            throw new DomainException(
                    "El monto del pago debe ser mayor a cero");
        }

        BigDecimal normalizedAmount =
                amount.setScale(2, RoundingMode.HALF_UP);

        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException(
                    "El monto del pago debe ser mayor a cero");
        }

        return normalizedAmount;
    }
}
