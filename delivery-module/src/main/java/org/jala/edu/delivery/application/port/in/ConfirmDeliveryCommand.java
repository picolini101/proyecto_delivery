package org.jala.edu.delivery.application.port.in;

/**
 * Closes the signed-in courier's shipment with the code given by the customer.
 */
public record ConfirmDeliveryCommand(String confirmationCode) {
}
