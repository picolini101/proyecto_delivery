package org.jala.edu.delivery.domain.model;

/**
 * Progress of one shipment the courier is carrying.
 * On the way, then picked up, then delivered. A shipment can also be cancelled.
 */
public enum DeliveryStep {
    ON_THE_WAY,
    PICKED_UP,
    DELIVERED,
    CANCELLED,
    DELEGATED
}
