package org.jala.edu.catalog.application.dto;

import java.util.List;

public record CatalogProductPage(
        List<CatalogProduct> products,
        int currentPage,
        int pageSize,
        int totalProducts,
        int totalPages,
        int totalAvailableProducts) {
}