package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

/**
 * @param requesterId usuario que solicita la eliminación (debe ser administrador activo)
 * @param productId   producto que se desea eliminar
 */
public record DeleteProductCommand(UUID requesterId, UUID productId) {
}