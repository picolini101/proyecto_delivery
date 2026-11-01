package org.jala.edu.catalog.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CommerceId(UUID value) {

    public CommerceId {
        Objects.requireNonNull(value, "Commerce id cannot be null");
    }

    public static CommerceId newId() {
        return new CommerceId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
