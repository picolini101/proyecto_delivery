package org.jala.edu.order.domain.model;

import org.jala.edu.core.domain.exception.DomainException;

import java.util.Objects;

public record OrderLine(
        ProductId productId,
        String productName,
        Money unitPrice,
        Quantity quantity,
        Money subtotal
) {
    public OrderLine {
        Objects.requireNonNull(productId, "ProductId no puede ser nulo");
        Objects.requireNonNull(productName, "ProductName no puede ser nulo");
        Objects.requireNonNull(unitPrice, "UnitPrice no puede ser nulo");
        Objects.requireNonNull(quantity, "Quantity no puede ser nula");
        Objects.requireNonNull(subtotal, "Subtotal no puede ser nulo");
    }
    public static OrderLine of(ProductId productId, String productName, Money unitPrice, Quantity quantity) {
        Money lineSubtotal = unitPrice.multiply(quantity);
        return new OrderLine(productId, productName, unitPrice, quantity, lineSubtotal);
    }
}
