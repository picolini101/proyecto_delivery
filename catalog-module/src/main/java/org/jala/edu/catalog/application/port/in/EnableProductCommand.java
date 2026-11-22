package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

/**
 * @param requesterId usuario que solicita habilitar el producto
 * @param productId producto que se desea habilitar
 */
public record EnableProductCommand(UUID requesterId, UUID productId) {
}
