package org.jala.edu.order.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ProductId(UUID value) {
    public ProductId {
        Objects.requireNonNull(value, "ProductId no puede ser nulo");
    }
    public static ProductId of(UUID value) {
        return new ProductId(value);
    }
    public static ProductId of(String value) {
        return new ProductId(UUID.fromString(value));
    }
    @Override
    public String toString() {
        return value.toString();
    }
}