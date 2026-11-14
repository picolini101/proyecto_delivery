package org.jala.edu.order.domain.model;

import java.util.Objects;
import java.util.UUID;

public record OrderStatusChangeId(UUID value) {

    public OrderStatusChangeId {
        Objects.requireNonNull(value, "OrderStatusChangeId no puede ser nulo");
    }

    public static OrderStatusChangeId newId() {
        return new OrderStatusChangeId(UUID.randomUUID());
    }

    public static OrderStatusChangeId of(UUID value) {
        return new OrderStatusChangeId(value);
    }

    public static OrderStatusChangeId of(String value) {
        return new OrderStatusChangeId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}