package org.jala.edu.catalog.application.dto;

import org.jala.edu.catalog.domain.model.Product;

public record ManagedProduct(Product product, String commerceName, String availability) {
}