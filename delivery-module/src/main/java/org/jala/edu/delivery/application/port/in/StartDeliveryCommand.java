package org.jala.edu.delivery.application.port.in;

import java.util.UUID;

/**
 * Starts the shipment of an order the signed-in courier already took.
 */
public record StartDeliveryCommand(UUID orderId) {
}
