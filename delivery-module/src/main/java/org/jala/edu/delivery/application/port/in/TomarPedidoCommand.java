package org.jala.edu.delivery.application.port.in;

import java.util.UUID;

/**
 * Takes one ready order for the signed-in courier.
 */
public record TomarPedidoCommand(UUID orderId) {
}
