package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.List;
import org.jala.edu.catalog.application.dto.CatalogProduct;

final class CatalogEmptyState {

    private CatalogEmptyState() {
    }

    static boolean hasNoAvailableProducts(List<CatalogProduct> products) {
        return products.stream().noneMatch(CatalogProduct::available);
    }
}