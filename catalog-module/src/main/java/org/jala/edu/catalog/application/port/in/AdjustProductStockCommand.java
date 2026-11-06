package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

/**
 * @param requesterId usuario que realiza la operación
 * @param productId producto cuyo stock se desea ajustar
 * @param quantity nueva cantidad de stock
 */
public record AdjustProductStockCommand(
        UUID requesterId,
        UUID productId,
        int quantity) {
}