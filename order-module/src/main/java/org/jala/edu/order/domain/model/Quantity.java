package org.jala.edu.order.domain.model;

import org.jala.edu.order.domain.exception.InvalidQuantityValueException;

public record Quantity(int value) {
    public Quantity {
        if (value <= 0) {
            throw new InvalidQuantityValueException(value);
        }
    }
    public static Quantity of(int value) {
        return new Quantity(value);
    }
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}