package org.jala.edu.catalog.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogProduct(
        UUID id,
        String name,
        String category,
        BigDecimal price,
        boolean available) {
}