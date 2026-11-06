package org.jala.edu.catalog.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @param requesterId el usuario que realiza la operación; verificado a través de {@code UserService}
 * @param commerceId  el comercio al que pertenecerá el producto
 */
public record CreateProductCommand(
        UUID requesterId,
        UUID commerceId,
        String name,
        String category,
        BigDecimal price) {
}