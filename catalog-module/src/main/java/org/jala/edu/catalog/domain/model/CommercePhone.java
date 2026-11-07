package org.jala.edu.catalog.domain.model;

import java.util.regex.Pattern;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * Teléfono de un comercio en formato internacional E.164: «+», código de país y número,
 * máximo 15 dígitos y sin empezar en 0 (ej. {@code +573001234567}). Es obligatorio.
 */
public record CommercePhone(String value) {

    private static final Pattern PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    public CommercePhone {
        if (value == null || value.isBlank()) {
            throw new DomainException("Commerce phone cannot be blank");
        }
        value = value.strip();
        if (!PATTERN.matcher(value).matches()) {
            throw new DomainException("Commerce phone has an invalid format: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}