package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

public record ListAvailableProductsQuery(
        UUID commerceId,
        int page) {
}