package org.jala.edu.order.domain.model;

import java.util.UUID;

public class CartItem {

    private final UUID productId;
    private final UUID commerceId;
    private int quantity;

    public CartItem(UUID productId, UUID commerceId, int quantity) {
        this.productId = productId;
        this.commerceId = commerceId;
        this.quantity = quantity;
    }

    public UUID getProductId() { return productId; }
    public UUID getCommerceId() { return commerceId; }
    public int getQuantity() { return quantity; }

    void setQuantity(int quantity) { this.quantity = quantity; }
}
