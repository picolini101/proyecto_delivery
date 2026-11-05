package org.jala.edu.payment.domain.model;

import org.jala.edu.core.domain.exception.DomainException;
import java.util.Objects;

public final class Alias {

    private final String value;

    public Alias(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException("El alias no puede estar vacío");
        }
        String trimmed = value.trim();
        if (trimmed.length() < 3 || trimmed.length() > 30) {
            throw new DomainException("El alias debe tener entre 3 y 30 caracteres");
        }
        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alias alias = (Alias) o;
        return Objects.equals(value, alias.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}