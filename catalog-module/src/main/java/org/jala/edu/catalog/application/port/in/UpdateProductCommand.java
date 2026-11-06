package org.jala.edu.catalog.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Datos editables de un producto existente.
 *
 * @param requesterId usuario que realiza la operación; verificado a través de {@code UserService}
 * @param productId   producto que se desea modificar
 */
public record UpdateProductCommand(
        UUID requesterId,
        UUID productId,
        String name,
        String category,
        BigDecimal price) {
}