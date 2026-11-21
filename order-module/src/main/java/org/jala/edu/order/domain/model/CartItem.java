package org.jala.edu.order.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class CartItem {

    private final UUID productId;
    private final UUID commerceId;
    private int quantity;
    /** Precio de una unidad que el cliente vio al agregarlo (o al aceptar un cambio); null si no se guardo. */
    private BigDecimal unitPrice;

    public CartItem(UUID productId, UUID commerceId, int quantity) {
        this(productId, commerceId, quantity, null);
    }

    public CartItem(UUID productId, UUID commerceId, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.commerceId = commerceId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public UUID getProductId() { return productId; }
    public UUID getCommerceId() { return commerceId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }

    void setQuantity(int quantity) { this.quantity = quantity; }
    void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}