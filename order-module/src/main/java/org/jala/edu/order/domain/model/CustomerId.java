package org.jala.edu.order.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CustomerId(UUID value) {
    public CustomerId {
        Objects.requireNonNull(value, "CustomerId cannot be null");
    }
    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }
    public static CustomerId of(String value) {
        return new CustomerId(UUID.fromString(value));
    }
    @Override
    public String toString() {
        return value.toString();
    }
}
