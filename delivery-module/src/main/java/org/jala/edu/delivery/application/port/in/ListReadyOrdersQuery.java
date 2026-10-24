package org.jala.edu.delivery.application.port.in;

import java.util.UUID;

/**
 * Asks for the orders a courier may pick up. The courier id is checked against {@code UserService}.
 */
public record ListReadyOrdersQuery(UUID courierId) {
}
