package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

/**
 * @param requesterId usuario que solicita deshabilitar el producto
 * @param productId producto que se desea deshabilitar
 */
public record DisableProductCommand(UUID requesterId, UUID productId) {
}