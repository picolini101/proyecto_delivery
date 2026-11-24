package org.jala.edu.delivery.domain.model;

import java.util.Objects;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * Four-digit code the customer gives the courier to close the delivery.
 */
public record ConfirmationCode(String value) {

    public ConfirmationCode {
        Objects.requireNonNull(value, "Confirmation code is required");
        if (!value.matches("\\d{4}")) {
            throw new DomainException("Confirmation code must be 4 digits");
        }
    }
}
