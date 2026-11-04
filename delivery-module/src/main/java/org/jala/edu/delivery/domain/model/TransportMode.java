package org.jala.edu.delivery.domain.model;

/**
 * Permitted modes of transport for a delivery person
 */
public enum TransportMode {

    BICYCLE("Bicicleta"),
    MOTORCYCLE("Motocicleta"),
    ON_FOOT("A pie");

    private final String label;

    TransportMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}