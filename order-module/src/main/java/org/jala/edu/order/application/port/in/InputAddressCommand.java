package org.jala.edu.order.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record InputAddressCommand(UUID orderId, UUID customerId, String city, String avenue, String houseNumber) {
    public InputAddressCommand {
        Objects.requireNonNull(orderId, "OrderId es obligatorio");
        Objects.requireNonNull(customerId, "CustomerId es obligatorio");
    }
}
