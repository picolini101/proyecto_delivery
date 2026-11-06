package org.jala.edu.catalog.application.port.in;

import java.util.UUID;
import org.jala.edu.catalog.domain.model.ProductStatus;

public record UpdateProductStatusCommand(
        UUID requesterId,
        UUID productId,
        ProductStatus status) {
}
