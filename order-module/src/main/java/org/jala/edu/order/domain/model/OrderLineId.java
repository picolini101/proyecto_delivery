package org.jala.edu.order.domain.model;

import java.util.Objects;
import java.util.UUID;

public record OrderLineId(UUID value) {

    public OrderLineId {
        Objects.requireNonNull(value, "OrderLineId no puede ser nulo");
    }

    public static OrderLineId newId() {
        return new OrderLineId(UUID.randomUUID());
    }

    public static OrderLineId of(UUID value) {
        return new OrderLineId(value);
    }
}
