package org.jala.edu.order.domain.model;

import org.jala.edu.core.domain.exception.DomainException;

/** Validated delivery address supplied during checkout. */
public record DeliveryAddress(String city, String avenue, String houseNumber) {
    public DeliveryAddress {
        city = required(city, "ciudad");
        avenue = required(avenue, "avenida");
        houseNumber = required(houseNumber, "número de casa");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El campo " + field + " es obligatorio");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return avenue + " " + houseNumber + ", " + city;
    }
}
